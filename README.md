# Tech Layoof — API

Backend do **Tech Layoof**, um agregador de notícias de demissões (*layoffs*) no
setor de tecnologia. A proposta do produto é reunir num só lugar os anúncios de
corte espalhados por portais de notícia, estruturar cada caso (empresa, número de
desligados, local, data, fonte) e avisar quem se cadastrou.

Este repositório contém a **API REST em Java/Spring Boot**, responsável pelas
contas e pelo acesso à plataforma: cadastro com e-mail e senha, login com Google,
autenticação stateless por JWT, recuperação de senha por código de verificação,
gestão do próprio perfil e o envio dos e-mails transacionais — cada disparo fica
registrado no banco para auditoria.

---

## Tecnologias

| Camada | Stack |
|---|---|
| Linguagem / runtime | **Java 21** |
| Framework | **Spring Boot 4.1** — Web, Data JPA, Security, Validation, Mail |
| Banco | **PostgreSQL** (Hibernate, `ddl-auto=update`) |
| Token | **Auth0 java-jwt 4.5** (HS256) |
| Login social | **Google API Client 2.7** (verificação de ID token) |
| Mapeamento | **MapStruct 1.6.3** + **Lombok 1.18.46** |
| Testes | **JUnit 5**, **Mockito**, **AssertJ**, `spring-security-test` |
| Build | **Maven** (com wrapper) |
| Infra local | **Docker Compose** (PostgreSQL) |
| IA | **anthropic-java 2.52** — declarada para a etapa de extração dos dados |

---

## Como rodar

### Pré-requisitos
- **JDK 21**
- **Docker** (ou um PostgreSQL já instalado)

### 1. Variáveis de ambiente

Copie o exemplo e preencha:

```bash
cp .env.example .env
```

O arquivo é carregado automaticamente pela aplicação
(`spring.config.import=optional:file:.env[.properties]`).

| Variável | Obrigatória | Para que serve |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | sim | Conexão com o PostgreSQL |
| `LAYOOF_JWT_SECRET` | **sim** | Assinatura do JWT — sem ela a aplicação não sobe |
| `GOOGLE_CLIENT_ID` | só para login Google | Client ID do tipo *Aplicativo Web* |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | só para enviar e-mail | Credenciais SMTP |
| `MAIL_ENABLED` | não | `false` só loga o e-mail, sem conectar no SMTP — ideal em dev |
