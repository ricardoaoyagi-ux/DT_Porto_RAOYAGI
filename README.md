# Teste Tecnico - Microservico CNAE

Projeto Spring Boot criado para avaliacao de candidatos a vagas de Lider Tecnico e Analista.

## Stack

- Java 25
- Spring Boot 4.1.0
- Spring Web
- Spring Data JPA
- H2 Database
- Lombok
- Maven

## Como executar

```bash
mvn spring-boot:run
```

Com o perfil de desenvolvimento (habilita o H2 Console):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Testes:

```bash
mvn clean test
```

H2 Console (somente no perfil `dev`):

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:cnaedb
User: sa
Password:
```

## Endpoints

```http
GET /api/cnaes
GET /api/cnaes/buscar?termo=programas
GET /api/cnaes/codigo?codigo=6201-5/01
GET /api/cadastros-secundarios
GET /api/cadastros-secundarios/validar-cnae?codigoCnae=6201-5/01
POST /api/cadastros-secundarios
```

Comportamento esperado:

- `GET /api/cnaes` deve retornar todas as atividades cadastradas.
- `GET /api/cnaes/buscar?termo={texto}` deve buscar CNAEs que contenham o texto informado em qualquer parte da descricao, ignorando maiusculas e minusculas.
- `GET /api/cnaes/codigo?codigo={codigo}` deve retornar o CNAE do codigo informado.
- Codigos CNAE inexistentes devem retornar uma resposta HTTP adequada para recurso nao encontrado.
- `POST /api/cadastros-secundarios` deve criar um cadastro vinculado a um CNAE existente.
- `GET /api/cadastros-secundarios/validar-cnae?codigoCnae={codigo}` deve validar se o CNAE informado pode ser usado no cadastro.
- Cadastros secundários com CNAE inexistente nao devem ser criados.

Exemplo:

```bash
curl http://localhost:8080/api/cnaes
curl "http://localhost:8080/api/cnaes/buscar?termo=programas"
curl "http://localhost:8080/api/cnaes/codigo?codigo=6201-5/01"
curl -X POST http://localhost:8080/api/cadastros-secundarios \
  -H "Content-Type: application/json" \
  -d '{"nomeFantasia":"Tech Porto","documento":"12345678000199","codigoCnae":"6201-5/01"}'
```

## Desafio para o candidato

Objetivo:

1. Fazer a aplicacao subir corretamente.
2. Validar os endpoints disponiveis.
3. Identificar e corrigir problemas encontrados durante a execucao.
4. Explicar as causas dos problemas e as decisoes tomadas.
5. Adicionar ou ajustar testes, quando fizer sentido.

## Entrega esperada

- Codigo corrigido em um branch ou pull request.
- Breve explicacao tecnica das alteracoes.
- Evidencias de execucao, como comandos usados, respostas dos endpoints ou testes.

## Alterações realizadas

Cada problema abaixo foi reproduzido com `curl` antes da correção e testado de novo depois dela. O documento de
evidências traz as capturas de tela, com cenários válidos e inválidos para todos os endpoints, e a execução dos testes:

📄 **[Memória técnica e evidências (PDF)](DESAFIO%20TECNICO%20PORTO%20SEGURO%20-%20RICARDO_AOYAGI.pdf)**

### 1. Banco de dados

| Problema | Causa | Correção |
|---|---|---|
| `/h2-console` retornava **404** | No Spring Boot 4 o H2 Console virou um módulo separado (`spring-boot-h2console`), que não estava no `pom.xml` | Incluí o módulo. Por segurança, o console fica **desabilitado** no `application.properties` e só é habilitado no perfil `dev` (`application-dev.properties`), porque não deve ser exposto em produção |

As credenciais do H2 (`sa`/senha vazia) são as do exercício. Em um ambiente real, devem vir de variáveis de ambiente
ou de um gerenciador de secrets.

### 2. APIs

**Endpoints sem alteração.** Foram testados com e sem dados e já se comportavam como esperado:

| Endpoint | Comportamento esperado | Resultado |
|---|---|---|
| `GET /api/cnaes` | Retorna todas as atividades cadastradas | ✅ OK: lista vazia sem dados, todos os CNAEs com dados |
| `GET /api/cadastros-secundarios` | Lista todos os cadastros secundários | ✅ OK: lista vazia sem dados, todos os cadastros com dados |

**Endpoints corrigidos:**

| Endpoint | Problema | Causa | Correção |
|---|---|---|---|
| `GET /api/cnaes/buscar` | Só encontrava descrições que **começavam** com o termo. Ex.: `termo=programas` retornava `[]` | A query JPQL usava `like concat(:termo, '%')`, sem o `%` inicial | Troquei por `like lower(concat('%', :termo, '%'))` em `AtividadeEconomicaCnaeRepository` |
| `GET /api/cnaes/codigo` | Código inexistente retornava **200 com o CNAE de id 1**. Com a tabela vazia, retornava **500** | `.orElseGet(() -> repository.findAll().getFirst())` devolvia o primeiro registro quando o código não existia | `.orElseThrow(() -> new CnaeNaoEncontradoException(codigo))`, convertida em **404** por um `@RestControllerAdvice` (`GlobalExceptionHandler`) |
| `GET /api/cadastros-secundarios/validar-cnae` | Mesmo comportamento: **200 com o CNAE de id 1**, ou 500 com a tabela vazia | Mesmo `orElseGet` com `findAll().getFirst()` | Mesma exceção, com retorno **404** |
| `POST /api/cadastros-secundarios` | CNAE inexistente gerava cadastro **gravado com o primeiro CNAE da tabela** (201) | Mesmo `orElseGet`, agora levando a dados inconsistentes no banco | A exceção é lançada antes do `save`, e o cadastro não é criado (**404**). Os dois métodos duplicados (`buscarCnaeParaCadastro` / `buscarCnaeParaValidacao`) viraram um só, `buscarCnaePorCodigo` |
| `POST /api/cadastros-secundarios` | Campo ausente no JSON retornava **500** (violação de NOT NULL no banco) | Não havia validação de entrada | Incluí `spring-boot-starter-validation`, `@NotBlank`/`@Size` em `CadastroSecundarioRequest` e `@Valid` no controller. O retorno passa a ser **400** com mensagens claras |

Os erros agora seguem um formato padrão (`ApiErrorResponse`):

```json
{ "status": 404, "error": "Not Found", "message": "CNAE não encontrado: 9999" }
```

Quando há vários campos inválidos, a resposta traz todas as mensagens em ordem fixa, por exemplo:
`"Código CNAE é obrigatório; Documento é obrigatório; Nome fantasia é obrigatório"`.

Os services passaram a ter `@Transactional(readOnly = true)`, e o `cadastrar` tem `@Transactional`.

Evidências (cenários válidos e inválidos):

```bash
# Sem alteração
curl -i http://localhost:8080/api/cnaes
curl -i http://localhost:8080/api/cadastros-secundarios

# Busca por descrição
curl -i "http://localhost:8080/api/cnaes/buscar?termo=programas"

# Busca por código (existente / inexistente)
curl -i "http://localhost:8080/api/cnaes/codigo?codigo=6201-5/01"
curl -i "http://localhost:8080/api/cnaes/codigo?codigo=9999"

# Validação de CNAE (existente / inexistente)
curl -i "http://localhost:8080/api/cadastros-secundarios/validar-cnae?codigoCnae=6201-5/01"
curl -i "http://localhost:8080/api/cadastros-secundarios/validar-cnae?codigoCnae=9999"

# Cadastro (válido / CNAE inexistente / campos ausentes)
curl -i -X POST http://localhost:8080/api/cadastros-secundarios   -H "Content-Type: application/json"   -d '{"nomeFantasia":"Tech Porto","documento":"12345678000199","codigoCnae":"6201-5/01"}'
curl -i -X POST http://localhost:8080/api/cadastros-secundarios   -H "Content-Type: application/json"   -d '{"nomeFantasia":"Tech Porto","documento":"12345678000199","codigoCnae":"9999"}'
curl -i -X POST http://localhost:8080/api/cadastros-secundarios   -H "Content-Type: application/json"   -d '{}'
```

| Chamada | Antes | Depois |
|---|---|---|
| `GET /api/cnaes` | 200, 8 CNAEs | 200, 8 CNAEs (sem alteração) |
| `GET /api/cadastros-secundarios` | 200, lista de cadastros | 200, lista de cadastros (sem alteração) |
| `buscar?termo=programas` | `[]` | 2 CNAEs (6201-5/01, 6202-3/00) |
| `codigo?codigo=6201-5/01` | 200 | 200 |
| `codigo?codigo=9999` | 200 "Cultivo de arroz" | 404 |
| `validar-cnae?codigoCnae=6201-5/01` | 200 | 200 |
| `validar-cnae?codigoCnae=9999` | 200 "Cultivo de arroz" | 404 |
| `POST` válido | 201 | 201 |
| `POST` com CNAE 9999 | 201, gravado com "Cultivo de arroz" | 404, nada gravado |
| `POST {}` | 500 | 400 |

### 3. Testes

O projeto original não tinha testes. Agora são **22 testes** (JUnit 5):

- **9 unitários** (Mockito, sem contexto Spring nem banco):
  - `AtividadeEconomicaCnaeServiceImplTest`: listagem, busca por descrição e por código, incluindo o código
    inexistente (`CnaeNaoEncontradoException`).
  - `CadastroSecundarioServiceImplTest`: cadastro válido, validação de CNAE, listagem e cadastro com CNAE
    inexistente. Nesse último cenário o teste confirma que o `save` **não** é chamado
    (`verify(repository, never()).save(any())`).
- **13 de integração** (`@SpringBootTest` + MockMvc + H2), cobrindo o fluxo Controller → Service → Repository →
  Banco:
  - `AtividadeEconomicaCnaeControllerIntegrationTest`: listagem com e sem dados, busca parcial sem diferenciar
    maiúsculas/minúsculas e ordenada por código, e 404 para código inexistente.
  - `CadastroSecundarioControllerIntegrationTest`: cadastro válido, 400 para campo obrigatório ausente e para vários
    campos inválidos, 404 para CNAE inexistente (sem persistir), validação de CNAE e listagem.
  - As classes de integração usam `@Transactional`, então cada teste desfaz o que gravou ao terminar. Isso deixa os
    testes independentes da ordem de execução (validado com `mvn test "-Dsurefire.runOrder=reversealphabetical"`).

## Melhorias futuras

- **Auditoria:** incluir campos como `criado_em`, `atualizado_em` e `usuario` nas tabelas.
- **Open Session in View e N+1:** os services agora usam `@Transactional(readOnly = true)` (e `@Transactional` na escrita), mas `spring.jpa.open-in-view` continua habilitado (padrão do Spring Boot). Em `GET /api/cadastros-secundarios`, o CNAE de cada cadastro é carregado de forma `LAZY`, gerando uma consulta extra por registro (N+1). Evolução sugerida: buscar o CNAE junto (`@EntityGraph(attributePaths = "cnae")` ou `join fetch` no repositório) e desabilitar `spring.jpa.open-in-view`.
