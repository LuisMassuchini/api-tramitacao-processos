# API de Tramitação de Processos — Digitalizando o Setor Público

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue?logo=java&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=spring&logoColor=white" alt="Spring Boot 3" />
  <img src="https://img.shields.io/badge/Spring_Security-6.x-blueviolet?logo=springsecurity&logoColor=white" alt="Spring Security 6" />
  <img src="https://img.shields.io/badge/JWT-Authentication-orange" alt="JWT" />
  <img src="https://img.shields.io/badge/MySQL-8.x-blue?logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/PDF-iText_7-red" alt="iText 7" />
</p>

Modernização de um módulo legado (PHP) para uma API RESTful segura, performática e integrada, eliminando papel, viabilizando assinatura eletrónica e gerando um rasto de auditoria digital via PDFs consolidados.

Palavras‑chave: Java 21, Spring Boot 3, Spring Security 6, JWT, RBAC, RESTful API, Hibernate 6, JPA, Specifications, MapStruct, iText 7, MySQL 8, Swagger/OpenAPI 3, Maven, DTO, Pagination, N+1, MultipleBagFetchException, ControllerAdvice, GlobalExceptionHandler, Multipart, File Upload, ZIP, Digital Signature.

## 🎯 Desafio
Digitalizar o fluxo de tramitação de processos (governo) originalmente em PHP procedural e fortemente dependente de papel — reduzindo custos, latência e riscos, e aumentando rastreabilidade e segurança.

## ✨ Solução
API REST moderna em Java/Spring Boot atuando como micro‑serviço acoplado ao frontend legado (PHP). Cada etapa do processo é registrada, assinada (desenho ou texto padronizado) e consolida uma nova página no PDF oficial do despacho, formando o “processo físico” digital.

> Sugestão: insira aqui um GIF curto da criação/encaminhamento/visualização do PDF (ex.: ScreenToGif).

## 🚀 Principais Funcionalidades
- Protocolo e despacho 100% digitais: cada nova etapa acrescenta uma página ao PDF consolidado (sem sobrescrever páginas anteriores)
- Assinatura eletrónica flexível: desenho (imagem base64) ou assinatura de texto padronizada
- Segurança com JWT (Bearer) e autorização com RBAC via `@PreAuthorize`
- Gestão de anexos: upload múltiplo, download individual e pacote `.zip`
- Filtros inteligentes e paginação: “Todos”, “Caixa de Entrada”, “Enviados”
- Documentação interativa (Swagger/OpenAPI)

## 💡 Desafios Técnicos e Soluções
- MultipleBagFetchException (Hibernate): consultas separadas para coleções (etapas/arquivos) + paginação estável
- Data truncation em campos longos: `@Column(columnDefinition = "TEXT")` (descrição/observação/assinatura)
- Normalização de autenticação: uso consistente de matrícula (subject) no JWT, fallback por UID quando necessário
- PDF robusto: sempre adicionar página nova; cabeçalho com logo; protocolo quando disponível; assinatura (imagem ou texto)
- Downloads resilientes: verificação de existência física e nomes de arquivo determinísticos

## 🛠️ Tecnologias
| Categoria | Stack |
| --- | --- |
| Linguagem & Framework | Java 21, Spring Boot 3 |
| Segurança | Spring Security 6 (JWT, RBAC) |
| Acesso a Dados | Spring Data JPA, Hibernate 6, Specifications |
| Banco | MySQL 8.x |
| Mapeamento | MapStruct |
| PDFs | iText 7 |
| Build | Maven |
| API Docs | Swagger/OpenAPI 3 |

## ⚙️ Como Executar
### TL;DR
1. Java 21, Maven 3.6+, MySQL 8.x
2. Crie o banco (ex.: `dbsisimprensa`)
3. Configure `src/main/resources/application-local.properties`
4. Rode com o perfil local
5. Acesse Swagger em http://localhost:9090/swagger-ui/index.html

### Passo a passo
1) Clone o repositório
```bash
git clone https://github.com/seu-usuario/seu-repositorio.git
cd seu-repositorio
```
2) Banco de dados
- Crie o schema `dbsisimprensa`
- Propriedades (arquivo: `src/main/resources/application-local.properties`):
```properties
server.port=9090
spring.datasource.url=jdbc:mysql://localhost:3306/dbsisimprensa?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=sua-senha-aqui
spring.jpa.hibernate.ddl-auto=update
file.upload-dir=./uploads/processos_tramitacao
application.security.jwt.secret-key=SuaChaveSecretaLongaAqui
```
3) Executar (perfil local)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
4) Swagger UI
- http://localhost:9090/swagger-ui/index.html

## 🔐 Autenticação (JWT)
- Envie `Authorization: Bearer <token>` em todas as requisições
- Exemplo:
```bash
curl -H "Authorization: Bearer <seu-token>" http://localhost:9090/api/processos
```

## 📄 Regras do PDF
- Uma etapa => uma nova página (append)
- Cabeçalho com logo + título
- “Processo nº”: usa `protocolo` se existir; caso contrário, pode exibir vazio ou fallback de ID conforme regra
- Assinatura: imagem base64 (desenho) OU bloco de texto padronizado; se nenhum, linha + nome/cargo centralizados
- Local de armazenamento: `uploads/processos_tramitacao/{processoId}/despacho_processo_{id}.pdf`

## 🧪 Troubleshooting (comum)
- 500 e "Unexpected token '<'" no frontend: backend retornou HTML; ver logs
- MultipleBagFetchException: não carregar 2 listas com JOIN FETCH; use consultas separadas
- Data truncation: anote campos longos com `@Column(columnDefinition = "TEXT")`
- NoResourceFound em download: confira rota e existência do arquivo
- Coluna inválida em ORDER BY: alinhar nomes (ex.: `data_criacao`)
- Repositório com campo inexistente: alinhar método ao atributo real (ex.: use `matricula` em vez de `login`)

## 📈 Impacto
- Redução significativa de papel e tempo de tramitação
- Rastro de auditoria digital (PDF consolidado)
- Segurança e governança com autenticação/autorização padronizadas

## 🤝 Contribuição
- Commits semânticos (feat/fix/docs/refactor/chore)
- PRs pequenos e com testes/ajustes de docs quando mudarem contratos

## 📜 Licenças
- Verifique a licença do iText 7 para uso em produção
- Defina a licença do projeto (MIT/Apache-2.0)

## 📞 Contato
- LinkedIn: [SEU-LINK-AQUI]
- Email: seu-email@exemplo.com
