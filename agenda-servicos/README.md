# Agenda+ — Sistema de Agenda de Serviços (Aula 7)

Aplicação web para controle de serviços e agendamentos, feita com **Spring Boot (MVC) + Spring Data JPA + H2 + Thymeleaf + Bootstrap**, seguindo o diagrama de classes e as telas do protótipo.

## Como executar

Requisitos: **Java 17 ou superior** e Maven (ou uma IDE com Maven embutido: IntelliJ, Eclipse/STS, VS Code com Extension Pack for Java).

```bash
mvn spring-boot:run
```

Ou, na IDE, abra a pasta do projeto como projeto Maven e rode a classe `AgendaServicosApplication`.

Acesse **http://localhost:8080** e entre com:

| Usuário   | Senha    | Tipo de acesso |
|-----------|----------|----------------|
| `marina`  | `123456` | Administrador  |
| `juliana` | `123456` | Profissional   |

Na primeira execução o sistema cria dados de exemplo (clientes, serviços e agendamentos, incluindo 3 para **hoje**, como no protótipo).
Para recomeçar do zero, pare a aplicação e apague a pasta `data/`.

### Banco H2

- Console: **http://localhost:8080/h2-console** (também no menu lateral → "Banco H2")
- JDBC URL: `jdbc:h2:file:./data/agenda` — usuário `sa`, senha em branco
- Consultas úteis para a apresentação:
  ```sql
  SELECT * FROM AGENDA;
  SELECT ID, USUARIO, SENHA FROM USUARIO;   -- senhas aparecem como hash BCrypt ($2a$...)
  SELECT * FROM CLIENTE c LEFT JOIN ENDERECO e ON e.ID = c.ENDERECO_ID;
  ```

### Testes automatizados

```bash
mvn test
```
`RegrasDeNegocioTest` testa as regras RN01–RN10 e `TelasTest` testa o login e abre todas as telas.

## Estrutura do projeto (camadas)

```
src/main/java/br/com/agendamais
├── entity/       Agenda, Servico, Cliente, Endereco, Usuario, TipoAcesso (JPA, conforme o diagrama)
├── repository/   Interfaces Spring Data JPA
├── service/      Regras de negócio (RN01–RN10) e BusinessException
├── controller/   Controllers MVC (CRUD de cada entidade + login)
├── dto/          Formulários (AgendamentoForm, UsuarioForm, FiltroAgenda)
└── config/       Configuração web (BCrypt, login), dados iniciais, formatação pt-BR
src/main/resources
├── templates/    Páginas Thymeleaf (login, agenda, clientes, serviços, usuários, endereços, tipos de acesso)
└── static/       CSS e JS próprios (Bootstrap é servido via WebJars, funciona sem internet)
```

### Relacionamentos (JPA)

| Entidade | Relacionamento |
|---|---|
| Agenda → Servico | `@ManyToOne` (obrigatório) |
| Agenda → Cliente | `@ManyToOne` (obrigatório) |
| Agenda → Usuario | `@ManyToOne` (obrigatório — é o profissional do atendimento) |
| Cliente → Endereco | `@OneToOne` com cascade e remoção de órfão |
| Usuario → TipoAcesso | `@ManyToOne` (obrigatório) |

O campo `Agenda.data` é um `LocalDateTime` (data **e** horário), como pede o enunciado.

## Regras de negócio (todas na camada Service)

| Regra | Onde | Como demonstrar na tela |
|---|---|---|
| RN01 Agendamento futuro | `AgendaService.salvar` | Novo agendamento com data de ontem |
| RN02 Conflito de agendamento | `AgendaService.salvar` (+ unique `usuario_id, data`) | Agendar Juliana para amanhã às 10:00 (já existe) |
| RN03 Valor do serviço > 0 | `ServicoService.salvar` | Novo serviço com valor 0 ou negativo |
| RN04 Cliente obrigatório | `AgendaService.salvar` | Salvar agendamento sem escolher cliente |
| RN05 Exclusão de serviço | `ServicoService.excluir` | Excluir "Corte feminino" |
| RN06 Exclusão de cliente | `ClienteService.excluir` | Excluir "Ana Martins" |
| RN07 E-mail válido e único | `ClienteService.salvar` | Cadastrar cliente com `teste@` ou com `ana.martins@email.com` |
| RN08 Tipo de acesso válido | `UsuarioService.salvar` | Salvar usuário sem tipo de acesso |
| RN09 Senha com BCrypt | `UsuarioService.salvar` e `UsuarioService.autenticar` | Ver a coluna `SENHA` no console do H2 |
| RN10 Pesquisa | `AgendaService.pesquisar` (Specification) | Agenda → "Pesquisar" por cliente, serviço ou período |

Os formulários usam `novalidate`, então as mensagens que aparecem vêm do **backend** (útil para a apresentação).

## Telas

- **Login, Agenda, Novo agendamento e Clientes** seguem o protótipo. Na Agenda, a lista pode ser vista por *Hoje · Semana · Mês*.
- **Serviços, Usuários, Tipos de acesso e Endereços** foram criadas no mesmo estilo para completar o CRUD das seis entidades.
- Cada item das listas tem um menu (⋮) com **Editar** e **Excluir** (com confirmação).

Ajustes em relação ao protótipo, para respeitar o diagrama de classes:
- "Profissional" no agendamento é o `Usuario` da Agenda.
- "Valor" vem do serviço escolhido (a Agenda não tem campo valor no diagrama), por isso é somente leitura.
- "Observações" é o campo `descricao` da Agenda.
- "Duração estimada" virou um resumo de data/horário/profissional, pois o serviço não tem duração no diagrama.
- Os horários oferecidos (08:00–17:30, de 30 em 30 min) podem ser alterados em `application.properties`.

