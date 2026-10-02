# Disaster Response Coordination — Distributed Systems

Academic client/server application developed in Java for the **Distributed Systems** course (2024/2025). The project simulates communication and coordination between entities involved in emergency-response scenarios such as natural disasters.

The original assignment was completed as a group project. This public portfolio version keeps the original architecture and core implementation while applying a small number of maintenance, privacy and robustness corrections after submission.

## Main features

- TCP client/server communication using Java sockets.
- Concurrent handling of multiple clients, with one server-side thread per connection.
- User registration and authentication with four profiles: `alto`, `medio`, `baixo` and `geral`.
- Hierarchical approval workflows for:
  - mass evacuation requests — approval by a high-level profile;
  - emergency communication requests — approval by a medium-level profile;
  - emergency resource requests — approval by a low-level profile.
- Communication groups with membership, approval and persistent group-message history.
- Direct user-to-user messages with persistent history, including messages sent while the recipient is offline.
- Persistent emergency notifications shown to users after login.
- Periodic server reports for high-level profiles.
- Local file-based persistence for users, requests, operations, messages and notifications.
- Password storage using PBKDF2-HMAC-SHA256 with an individual salt.

## Technologies and concepts

- Java
- TCP sockets
- Multithreading
- Shared-resource synchronization
- Client/server architecture
- File persistence
- Authentication and role-based authorization
- PBKDF2 password hashing

## Requirements

- **JDK 11 or newer**. JDK 17+ is recommended.
- No external libraries are required.

## Compile

From the project directory:

```bash
javac *.java
```

## Run

Start the server in one terminal:

```bash
java MultiServer
```

Then start one or more clients in separate terminals:

```bash
java Client
```

By default, the application uses TCP port **666** and connects to the local machine.

### Demo accounts

The repository contains generic demonstration accounts only:

| Profile | Email | Password |
| --- | --- | --- |
| High | `coordenador@example.com` | `123456` |
| Medium | `operador@example.com` | `123456` |
| Low | `agente@example.com` | `123456` |
| General | `utilizador@example.com` | `123456` |

The passwords are stored in `user.txt` as PBKDF2 hashes, not as plaintext.

## Project structure

The project intentionally keeps the simple structure used in the academic submission.

- `MultiServer.java` — server entry point, connected-user state and periodic reports.
- `Client.java` — terminal client and server-message receiver.
- `ProcessaCliente.java` — registration, authentication and profile-specific workflows.
- `ProcessaGrupos.java` / `BDGroups.java` — communication groups and persistence.
- `ProcessaMensagens.java` / `BDDirectMessages.java` — direct messages and history.
- `ProcessaEvacuacoes.java` — evacuation requests and approval workflow.
- `ProcessaComunicacoes.java` — emergency communication requests.
- `ProcessaRecursos.java` — emergency-resource requests.
- `NotificationService.java` / `BDNotifications.java` — online and persisted alerts.
- `BDUsers.java` — user persistence, pending registrations and operation log.
- `PasswordUtils.java` — password hashing and verification.
- `docs/PROTOCOL.md` — description of the line-oriented socket protocol.

The files `dangers.txt`, `zonas.txt`, `comunicacoes.txt` and `recursos.txt` contain the small catalogues used by the prototype. `user.txt` and `groups.txt` contain generic demonstration data. Operational history and pending-request files are generated locally at runtime and are excluded from Git.

## Communication protocol

The application uses a text-based, line-oriented protocol over a TCP socket. The server sends prompts and responses as newline-terminated text, while the client sends one command or value per line. Server notifications may be delivered asynchronously while the user is interacting with a menu.

See [`docs/PROTOCOL.md`](docs/PROTOCOL.md) for the commands, session flow and examples.

## Security note

This is an academic prototype rather than a production security system. Passwords are protected at rest with PBKDF2-HMAC-SHA256, but the TCP connection itself does **not** use TLS, so credentials and messages are not encrypted in transit.

## Portfolio maintenance

The portfolio version preserves the academic design while fixing issues that could affect normal use, including session cleanup, persisted identifiers, hierarchy checks, offline/direct messaging, password storage and runtime-file hygiene. These changes are maintenance of the original project rather than a redesign.

## Academic context

Developed for the **Distributed Systems** course during the 2024/2025 academic year. The assignment focused on sockets, multithreading, shared-resource control, authentication, persistent operation history and communication in a distributed emergency-coordination system.
