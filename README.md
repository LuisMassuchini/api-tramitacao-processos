# API de Tramitação de Processos — Digitalizando o Setor Público

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue?logo=java&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=spring&logoColor=white" alt="Spring Boot 3" />
  <img src="https://img.shields.io/badge/Spring_Security-6.x-blueviolet?logo=springsecurity&logoColor=white" alt="Spring Security 6" />
  <img src="https://img.shields.io/badge/JWT-Authentication-orange" alt="JWT" />
  <img src="https://img.shields.io/badge/MySQL-8.x-blue?logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/PDF-iText_7-red" alt="iText 7" />
</p>

Modernização de um módulo legado (PHP) para uma API RESTful segura, performática e integrada, eliminando papel, habilitando assinatura eletrónica e gerando rasto de auditoria digital via PDFs consolidados.

Palavras‑chave: Java 21, Spring Boot 3, Spring Security 6, JWT, RBAC, RESTful API, Hibernate 6, JPA, Specifications, MapStruct, iText 7, MySQL 8, Swagger/OpenAPI 3, Maven, DTO, Pagination, N+1, MultipleBagFetchException, ControllerAdvice, GlobalExceptionHandler, Multipart, File Upload, ZIP, Digital Signature.

## 📜 Sobre o Projeto
API que digitaliza a tramitação de processos internos, atuando como micro‑serviço acoplado ao frontend legado (PHP). Cada etapa é registrada, assinada (desenho ou texto padronizado) e acrescenta uma nova página ao PDF oficial de despacho (sem sobrescrita), formando o “processo físico” digital.

## 🎯 Desafio
Substituir rotinas em papel e PHP procedural por um backend moderno com segurança, rastreabilidade e integração contínua, sem interromper operações.

## ✨ Solução
- Arquitetura em camadas (Controller → Service → Repository) com DTOs (MapStruct)
- Segurança com Spring Security + JWT (RBAC via `@PreAuthorize`)
- JPA/Hibernate com Specifications e consultas otimizadas
- Geração/append de PDF por etapa (iText 7), upload/download de anexos e pacote `.zip`

## 🚀 Principais Funcionalidades
- Protocolo e despacho digitais: cada etapa adiciona nova página ao PDF consolidado
- Assinatura eletrónica flexível: desenho (base64) ou texto padronizado
- Filtros e paginação: “Todos”, “Caixa de Entrada”, “Enviados”
- Upload múltiplo, download individual e `.zip` de anexos
- Documentação interativa (Swagger/OpenAPI)

## 💡 Desafios Técnicos e Soluções
- MultipleBagFetchException: carregamento de coleções em consultas separadas para paginação estável
- Data truncation (texto longo): `@Column(columnDefinition = "TEXT")` em descrição/observação/assinatura
- Autenticação consistente: subject do JWT como matrícula; fallback por UID quando aplicável
- PDF robusto: cabeçalho com logo redimensionado, protocolo quando existir, assinatura imagem/texto, append seguro
- Downloads confiáveis: verificação física e paths determinísticos

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
4. Rode com o perfil `local`
5. Acesse Swagger: http://localhost:9090/swagger-ui/index.html (ou /swagger-ui.html)

### Passo a passo
1) Clone o repositório
```bash
git clone https://github.com/seu-usuario/seu-repositorio.git
cd seu-repositorio
```
2) Banco de dados e propriedades (arquivo: `src/main/resources/application-local.properties`)
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
- Header obrigatório: `Authorization: Bearer <token>`
```bash
curl -H "Authorization: Bearer <seu-token>" http://localhost:9090/api/processos
```

## 📄 Regras do PDF
- Uma etapa ⇒ uma nova página (append)
- Cabeçalho com logo + título; protocolo quando existir (1º passo pode ficar vazio ou usar fallback de ID conforme regra)
- Assinatura: imagem base64 (desenho) OU bloco de texto padronizado; se nenhum, linha + nome/cargo centralizados
- Persistência: `uploads/processos_tramitacao/{processoId}/despacho_processo_{id}.pdf`

## 🧪 Troubleshooting (comum)
- 500 e "Unexpected token '<'" no frontend: backend retornou HTML; ver logs
- MultipleBagFetchException: não carregar 2 listas com JOIN FETCH; use consultas separadas
- Data truncation: anote campos longos com `@Column(columnDefinition = "TEXT")`
- NoResourceFound em download: conferir rota e existência do arquivo
- Coluna inválida em ORDER BY: alinhar nomes (ex.: `data_criacao`)
- Métodos de repositório: alinhar com atributos reais (ex.: `matricula` vs `login`)

## 📈 Impacto
- Menos papel e tempo de tramitação; mais auditoria e segurança
- Processo digital consolidado e auditável por PDF

## 🤝 Contribuição
- Commits semânticos (feat/fix/docs/refactor/chore)
- PRs pequenos com testes e docs quando mudar contratos

## 📜 Licenças
- Verifique a licença do iText 7 para produção
- Defina a licença do projeto (MIT/Apache-2.0)

## 📞 Contato
- LinkedIn:https://www.linkedin.com/in/luis-massuchini/- 
- Email: luis.massuchini@gmail.com
