# Client/Server Protocol

## Overview

The application communicates through a **TCP socket on port 666**. The protocol is intentionally simple and text based, matching the terminal-oriented academic prototype.

- Transport: TCP
- Encoding: Java's platform-default character encoding through `InputStreamReader` / `PrintWriter`
- Framing: one logical command, menu choice or field value per line
- Server responses: newline-terminated text
- Connection model: one server-side `ProcessaCliente` thread per connected client
- Asynchronous traffic: notifications and direct/group messages can be written to an authenticated client's socket while menu interaction is in progress

There is no binary framing, JSON envelope or HTTP layer.

## Initial session

After a TCP connection is accepted, the server sends:

```text
Indique se quer realizar registo, login ou sair no sistema:
```

The client sends one of the following commands:

```text
registo
login
sair
```

Commands are currently compared as literal lowercase strings by the server.

## Registration

### Client request

```text
registo
<name>
<email>
<password>
<profile>
```

Valid profiles are:

```text
alto
medio
baixo
geral
```

A `geral` registration becomes active immediately. Other profiles are stored as pending registrations and require approval by a high-level profile.

The password is converted to a PBKDF2 representation before being persisted.

## Login

### Client request

```text
login
<email>
<password>
```

On success, the server associates the authenticated email with the client's output stream and registers the socket with the notification service. The user then receives the menu corresponding to their profile.

A second simultaneous login with the same account is rejected.

## Profile menus

After authentication, most actions are selected by sending the numeric option shown by the server. Additional values requested by an operation are also sent one per line.

### High profile (`alto`)

Main responsibilities include group management, creating requests, approving evacuation requests, approving registrations, viewing logs and managing zones.

### Medium profile (`medio`)

Can manage groups, create requests and approve emergency-communication requests.

### Low profile (`baixo`)

Can manage groups, create requests and approve emergency-resource requests.

### General profile (`geral`)

Can create resource requests and use direct messaging.

## Hierarchical approvals

The portfolio implementation follows these approval levels:

| Operation | Required approver |
| --- | --- |
| Mass evacuation | `alto` |
| Emergency communications | `medio` |
| Emergency resources | `baixo` |

Request data is persisted locally until it is processed.

## Groups

Authenticated users with access to group management receive numeric options for:

```text
1  Create group
2  Edit group
3  Delete group
4  View groups
5  View/approve creation requests
6  Send a group message
7  View group-message history
0  Return
```

Group membership is represented by email addresses. Messages are persisted before/alongside online notification delivery so they remain available in history.

## Direct messages

The direct-message submenu provides:

```text
1  Send message
2  View message history
3  View registered users
0  Return
```

A direct message contains sender, recipient, timestamp and message body. Messages are persisted in `directMessages.txt`. If the recipient is currently online, the notification service also writes the message to that user's socket immediately.

This means offline recipients can still consult the message later through their history.

## Emergency notifications

Global emergency alerts are:

1. persisted in `notifications.txt`;
2. logged as an operation;
3. sent to currently authenticated users.

After login, recent persisted alerts are displayed to the user, which covers users who were offline when the alert was created.

## Periodic reports

The server uses a scheduled executor to generate a periodic status report. The report is sent to authenticated high-level users and contains information such as active-user and registered-user counts plus operation data maintained by the prototype.

## Session termination

At the initial session level:

```text
sair
```

closes only that client's connection; it does not stop the server.

Inside authenticated menus, selecting `0` logs the user out and returns control to the session flow. The server removes the email/socket from the authenticated-user and notification registries during logout/disconnection.

The standalone client also stops its local input loop if the user writes `sair` or `logout`.

## Persistence files

The following files are generated or updated while the server runs and are intentionally excluded from Git:

```text
log.txt
messages.txt
directMessages.txt
notifications.txt
pedidos.txt
pedidosComunicacoes.txt
pedidosRecursos.txt
pendingRequest.txt
pendingRequestGroups.txt
profileChangeRequests.txt
```

Static/demo files kept in the repository include:

```text
user.txt
groups.txt
dangers.txt
zonas.txt
comunicacoes.txt
recursos.txt
```

## Limitations

The protocol is suitable for demonstrating sockets and concurrent client handling, but it is not intended as a production network protocol:

- there is no TLS encryption;
- server prompts and asynchronous notifications share the same text stream;
- there are no message IDs, length prefixes or structured envelopes;
- error responses are human-readable strings rather than machine-readable codes.
