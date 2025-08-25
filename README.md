# API de Tramitação de Processos  
## Plataforma de Digitalização, Rastreabilidade e Auditoria de Fluxos Administrativos

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Security-JWT-6f42c1" />
  <img src="https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Build-Maven-orange" />
  <img src="https://img.shields.io/badge/Docs-OpenAPI%2FSwagger-blue" />
  <img src="https://img.shields.io/badge/PDF-iText7-red" />
</p>

> Modernização de módulo legado (PHP procedural) para uma **API REST** robusta em **Java / Spring Boot**, focada em: eliminação de papel, assinatura eletrónica, governança, segurança (JWT + RBAC), auditabilidade e performance. Fornece serviços a um frontend legado sem interromper operação (estratégia de migração incremental / strangler pattern).

---
## 🎯 Objetivos Estratégicos
- Substituir trâmite físico por fluxo 100% digital (paperless / sustentabilidade).
- Criar **trilha de auditoria imutável** (PDF consolidado incremental + timestamps + autorias).
- Reduzir acoplamento do sistema legado (microserviço isolado escalável horizontalmente).
- Implementar **segurança corporativa** (autenticação JWT, autorização por perfil / RBAC, segregação de responsabilidades).
- Garantir **observabilidade** (logging estruturado, mensagens de debug em pontos críticos, rastreabilidade por protocolo / ID).

---
## 🧱 Arquitetura e Boas Práticas
| Camada | Padrões / Técnicas | Benefícios |
|--------|--------------------|------------|
| Controller | DTOs, Validation (Bean Validation), @ControllerAdvice | Fronteira limpa / respostas padronizadas |
| Service | Regras de negócio, transações (@Transactional), isolamento | Fácil evolução / testes |
| Repository | Spring Data JPA, Specifications, Queries otimizadas | Filtros dinâmicos / menor N+1 |
| Mapeamento | MapStruct | Conversão DTO ↔ Entidade performática |
| Persistência | Flyway (migrações), MySQL | Versionamento de schema confiável |
| Segurança | Spring Security, JWT, RBAC (@PreAuthorize) | Mínimo privilégio / proteção do pipeline |
| Documentos | iText7 (append incremental) | Histórico jurídico / imutabilidade |

Principais princípios aplicados: **SOLID, Clean Code, Defensive Programming, Fail Fast, Minimização de Acoplamento, Paginação / Streaming controlado, Idempotência (criação de etapas), Boundary Validation**.

---
## 🔐 Segurança (Enterprise-Ready)
- Autenticação stateless com **JWT** (claims: matrícula, uid, perfil, exp).  
- Filtro customizado (`JwtAuthFilter`) → extração, validação e injeção de contexto de segurança.  
- **RBAC** granular via `@PreAuthorize` (ex.: apenas perfis de chefia criam processos iniciais).  
- Sanitização e validação de entrada (Bean Validation + trimming lógico em repositório).  
- Separação de responsabilidades entre autenticação e autorização.  

---
## 📄 Gestão de Despachos / PDF Incremental
Cada **etapa** adiciona uma nova página ao PDF consolidado (sem sobrescrever).  
Características:
- Append controlado copiando páginas pré-existentes + nova página (iText).  
- Cabeçalho institucional + logotipo + identificação de protocolo (ou fallback ID).  
- Assinatura eletrónica flexível:  
  - (A) Desenho (canvas → Base64 → embed em PNG).  
  - (B) Assinatura textual padronizada (“Documento assinado eletronicamente…”).  
- Preparado para futura marca d'água / hashing / QR-code de verificação.  

---
## ✍️ Fluxo de Assinatura Eletrónica
1. Frontend (PHP + JS) captura desenho (SignaturePad) OU seleciona assinatura textual.  
2. Envia DTO (`EtapaRequestDTO`) com `assinaturaImagemBase64` + flag `usarAssinaturaTexto`.  
3. Service decide estratégia → PdfGenerationService aplica template (centralização, linha de assinatura, data/hora).  
4. Persistência de metadados + arquivo físico organizado em diretório por ID de processo.  

---
## 🚀 Funcionalidades Principais
- CRUD de Processos com **paginação, filtros dinâmicos e múltiplos contextos (para mim / enviados / geral)**.  
- Workflow de **etapas encadeadas** (histórico ordenado).  
- Upload múltiplo (Multipart) + download individual + ZIP consolidado.  
- Geração automática / append de **PDF consolidado de tramitação**.  
- Assinatura eletrónica (imagem ou modo textual legal).  
- Campo de **protocolo oficial** definido em etapa de validação (atualiza páginas seguintes).  
- Tratamento global de erros com JSON consistente (timestamp, path, mensagem, código).  
- Otimizações contra `MultipleBagFetchException` (queries em duas fases).  
- Proteções contra truncamento (`@Column(columnDefinition="TEXT")` em campos extensos).  

---
## ⚙️ Stack / Tecnologias
| Categoria | Tecnologia / Biblioteca |
|-----------|-------------------------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.x |
| Segurança | Spring Security 6, JWT (jjwt) |
| Persistência | Spring Data JPA, Hibernate, Flyway |
| Banco | MySQL 8.x |
| Mapeamento | MapStruct 1.5.x + Lombok |
| PDF | iText7 |
| Documentação | OpenAPI (springdoc) |
| Build | Maven |
| Testes (expansível) | JUnit 5 / Mockito |

---
## 🧪 Pontos de Qualidade / Evolução
Implementado / Preparado para:  
- Paginação consistente em endpoints de listagem (Pageable).  
- Logging segmentado (DEBUG para autenticação / resolução de usuário).  
- Estrutura clara para ampliar **Observability** (futuro: tracing distribuído / Micrometer + Prometheus).  
- Tratamento explícito de exceções de negócio vs infraestrutura.  
- Diretórios de armazenamento segregados por processo (facilita auditorias / limpeza).  

Backlog sugerido (Roadmap técnico):  
- Adicionar camada de cache (ex.: Caffeine / Redis) para listas frequentes.  
- Implementar E2E tests (RestAssured) + Contract tests.  
- Adicionar verificação de integridade de PDFs (hash + assinatura digital ICP-Brasil).  
- Conteinerização via Docker + pipeline CI/CD (GitHub Actions).  
- Suporte a internacionalização (i18n).  
- Exportação de relatórios (CSV/Excel) com agregações.  

---
## 🛠️ Como Executar Localmente
### Pré‑requisitos
Java 21, Maven 3.6+, MySQL 8.x (UTF8MB4), servidor PHP legado (XAMPP/WAMP) se quiser integrar.

### 1. Clonar
```
git clone https://github.com/SEU-USUARIO/api-tramitacao-processos.git
cd api-tramitacao-processos
```
### 2. Configurar Banco / Migrations
Criar database:
```
CREATE DATABASE dbsisimprensa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Flyway executará migrations automaticamente no start.

### 3. Propriedades Locais
Criar `src/main/resources/application-local.properties` (gitignored):
```
spring.datasource.url=jdbc:mysql://localhost:3306/dbsisimprensa
spring.datasource.username=root
spring.datasource.password=SUASENHA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

application.security.jwt.secret-key=CHAVE_SUPER_SECRETA_TROCAR
```
Iniciar:
```
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
Swagger UI: http://localhost:9090/swagger-ui.html

### 4. Integração com Frontend PHP
- Frontend envia JWT no header `Authorization: Bearer <token>`.
- Endpoints multipart para criação: `/api/processos/com-arquivo` e etapas `/api/etapas/{processoId}`.

---
## 🔍 Endpoints (Resumo)
| Recurso | Método | Descrição |
|---------|--------|-----------|
| /api/auth/login | POST | Autenticação (gera JWT) |
| /api/processos | GET | Listagem paginada / filtros |
| /api/processos/{id} | GET | Detalhes + histórico |
| /api/processos/com-arquivo | POST | Criar processo (arquivos + 1ª etapa) |
| /api/etapas/{processoId} | POST | Adicionar etapa + append PDF |
| /api/arquivos/{id}/download | GET | Download arquivo individual |
| /api/processos/{id}/arquivos/download-zip | GET | ZIP de anexos |

(Ver documentação completa no Swagger.)

---
## 🧾 Estrutura de Diretórios (Essencial)
```
/ uploads/processos_tramitacao/<ID>/
   ├─ despacho_processo_<ID>.pdf   (consolidado)
   ├─ despacho_etapa_<N>.pdf       (intermediário / histórico técnico)
   └─ <anexos diversos>
```

---
## 🤝 Contribuição
1. Fork / branch feature  
2. `mvn -q -DskipTests package` para validar build  
3. Pull Request com descrição clara (inclua steps de reprodução / impacto)  

---
## 📌 Diferenciais Técnicos para Recrutadores
- Experiência em **modernização de legado** sem downtime (estratégia progressiva).  
- Foco em **segurança aplicada** (JWT, RBAC, validação).  
- Solução para **problemas clássicos de ORM** (MultipleBagFetchException, N+1).  
- **Automação documental** (PDF incremental + assinatura eletrónica).  
- Arquitetura preparada para **escala horizontal** (stateless + token).  
- Código pronto para evolução em **observabilidade, testes e CI/CD**.  

---
## 📬 Contato
| Canal | Informação |
|-------|------------|
| LinkedIn | (adicione aqui) |
| Email | (adicione aqui) |
| Localização | Brasil |

---
> Projeto desenvolvido como parte de iniciativa de transformação digital institucional – foco em eficiência, governança documental e redução de custos operacionais.
