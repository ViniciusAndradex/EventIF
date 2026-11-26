# EventIF

Projeto acadêmico da disciplina Tópicos em Java para a Web, voltado à divulgação de eventos, inscrições, presença e certificados. Esta etapa contém apenas a estrutura inicial, os fluxos de domínio e as credenciais dos três perfis ainda não foram implementados.

## Execução com Docker Compose

Pré-requisitos: Docker com daemon em execução e plugin Docker Compose. Copie os valores locais de exemplo e inicie os serviços:

```sh
cp .env.example .env
docker compose up --build
```

Acesse `http://localhost:8080/`. A porta externa da aplicação é configurável por `APP_PORT`; dentro do contêiner a aplicação usa a porta 8080. O PostgreSQL fica acessível apenas no localhost pela porta `POSTGRES_PORT` (5432 por padrão), e seus dados são preservados no volume `postgres_data`. A aplicação aguarda o healthcheck do banco antes de iniciar. Java, PostgreSQL e Hibernate usam UTC internamente. Horários de atividades digitados na interface são horários locais, comparados no fuso `APP_TIME_ZONE`, cujo padrão é `America/Fortaleza`. Configure o mesmo fuso para todos os horários cadastrados; alterá-lo muda a interpretação dos horários existentes, sem convertê-los.

O `.env.example` contém valores fictícios exclusivos de desenvolvimento; altere-os no `.env` conforme necessário e nunca use essa senha em produção. O arquivo `.env` é ignorado pelo Git. `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` configuram o banco; `APP_PORT` e `POSTGRES_PORT` configuram as portas externas. As credenciais do PostgreSQL são aplicadas na criação inicial do volume: alterá-las no `.env` não altera automaticamente um banco já inicializado.

O inicializador cria os usuários `participante@eventif.local`, `organizador@eventif.local` e `admin@eventif.local` com a senha definida em `SEED_PASSWORD`, armazenada com BCrypt. O valor do exemplo é apenas para desenvolvimento; defina outro no `.env`. Quem já possui um `.env` deve acrescentar essa variável. O seed não altera senhas de usuários existentes. A autenticação por email ainda não está implementada.

## Alternativa com Maven local

Pré-requisitos: Java 21, Maven 3.6.3 ou superior e PostgreSQL acessível. Para usar somente o banco do Compose, após criar `.env` como acima:

```sh
docker compose up -d db
set -a
. ./.env
set +a
export DB_URL="jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}"
export DB_USERNAME="$POSTGRES_USER"
export DB_PASSWORD="$POSTGRES_PASSWORD"
export TZ=UTC
mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Duser.timezone=UTC
```

Também é possível usar um PostgreSQL local existente, definindo `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` para esse servidor. A aplicação fica em `http://localhost:8080/`. Hibernate usa `ddl-auto=update`. Para compilar e executar os testes existentes: `mvn compile` e `mvn test`.

A autenticação, os usuários e os perfis definitivos ainda serão implementados; até lá, o Spring Security usa sua configuração provisória de usuário padrão. Não use essa configuração em produção.

## Estrutura e próximos passos

- `src/main/java/br/edu/ifce/eventif`: aplicação, controllers, services, repositories, entidades, DTOs, configuração, exceções e utilitários.
- `src/main/resources`: configuração, templates Thymeleaf e fragments; Bootstrap é servido localmente pelo WebJar.
- `docs/`: contexto mestre, requisitos, decisões e checklist de aceitação.

As regras, autenticação por email, catálogo, inscrições e certificados ainda estão pendentes. OpenHTMLtoPDF está incluído como dependência para a futura geração de certificados. Consulte `docs/project-context.md` antes de alterar comportamentos.
