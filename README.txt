Sistemas Distribuídos - protótipo cliente/servidor

Compilar:
  javac *.java

Iniciar o servidor:
  java MultiServer

Noutra consola, iniciar um ou mais clientes:
  java Client

O projeto está configurado para localhost na porta 666.

Utilizadores de demonstração:
  coordenador@example.com / 123456 / perfil alto
  operador@example.com    / 123456 / perfil medio
  agente@example.com      / 123456 / perfil baixo
  utilizador@example.com  / 123456 / perfil geral

Comandos iniciais do cliente:
  registo
  login
  sair

Os ficheiros TXT existentes na raiz são usados como persistência local do protótipo.

Funcionalidades adicionadas/corrigidas na versão de portfolio:
  - mensagens privadas persistentes entre utilizadores, mesmo offline
  - histórico de mensagens privadas e mensagens de grupo
  - alertas de emergência persistidos e apresentados após login
  - passwords guardadas com PBKDF2; ficheiros antigos em texto simples são migrados após login
