API de Tramitação de Processos - Modernização de Sistema Legado
📜 Sobre o Projeto
Este projeto consiste na criação de uma API RESTful robusta e segura, desenvolvida com Java e Spring Boot, para gerenciar o fluxo de tramitação de processos internos. O principal objetivo foi modernizar um módulo existente de um sistema legado em PHP procedural, visando a transformação digital e a redução drástica do uso de papel.

A nova API atua como um micro-serviço desacoplado, que se integra ao sistema PHP existente, permitindo uma migração gradual e segura, sem interromper as operações atuais. A solução final é um sistema híbrido onde o frontend legado (PHP) consome os dados e executa as regras de negócio através da nova API Java.

✨ Jornada de Desenvolvimento e Arquitetura
A elaboração deste sistema seguiu uma jornada incremental, focada em construir uma base sólida e adicionar funcionalidades complexas de forma progressiva.

Análise Inicial: O ponto de partida foi a análise do sistema PHP existente e da estrutura do banco de dados MySQL, identificando as entidades principais (Processo, Etapa, Arquivo, Usuário) e as regras de negócio.

Arquitetura da API: Foi definida uma arquitetura em camadas (Controller, Service, Repository) para garantir a separação de responsabilidades. O padrão DTO (Data Transfer Object) com MapStruct foi adotado para desacoplar a lógica de negócio das entidades do banco de dados.

Segurança em Primeiro Lugar: A segurança foi implementada usando Spring Security com autenticação baseada em Tokens JWT (JSON Web Tokens). O sistema PHP foi modernizado para gerar os tokens no momento do login, que são então validados pela API Java a cada requisição.

Controle de Acesso (RBAC): Foram implementadas regras de autorização granulares com anotações @PreAuthorize, restringindo ações críticas (como a criação de processos) apenas a perfis de utilizador específicos (Role-Based Access Control).

Funcionalidades de Workflow:

Criação de Processos e Etapas: Implementação de endpoints para criar processos e adicionar etapas sequenciais, formando um histórico de tramitação.

Gestão de Anexos: Suporte para upload de múltiplos ficheiros (multipart/form-data), download individual e a capacidade de baixar todos os anexos de um processo como um ficheiro .zip.

Geração Automática de PDFs: Utilizando a biblioteca iText, o sistema gera automaticamente um PDF de despacho consolidado para cada processo. A cada nova etapa, uma nova página é adicionada ao PDF, criando um registo de auditoria formal e imutável.

Assinatura Eletrónica: O sistema permite que os utilizadores desenhem as suas assinaturas, que são capturadas como imagens e inseridas nos PDFs de despacho, adicionando uma camada de formalidade.

Otimização e Eficiência: Foram implementadas consultas avançadas com Spring Data JPA Specifications para filtros dinâmicos e JPQL com JOIN FETCH para otimizar a busca de dados complexos, resolvendo o problema de N+1 queries.

Integração com Frontend: As páginas PHP legadas foram refatoradas para utilizar JavaScript (Fetch API), consumindo os endpoints da nova API para exibir e manipular os dados de forma dinâmica, sem recarregar a página.

🛠️ Tecnologias Utilizadas
Backend
Java 21

Spring Boot 3

Spring Security (Autenticação JWT e Autorização baseada em Perfis)

Spring Data JPA & Hibernate (Persistência de Dados)

MySQL (Banco de Dados)

MapStruct (Mapeamento de DTOs)

iText (Geração e Manipulação de PDFs)

Maven (Gestão de Dependências)

Frontend (Legado e Integração)
PHP

JavaScript (ES6+) com Fetch API (AJAX)

HTML5

Bootstrap

Ferramentas
Git & GitHub (Controlo de Versão)

Postman / Swagger UI (Teste e Documentação de API)

IntelliJ IDEA (IDE)

🚀 Principais Funcionalidades
✅ Autenticação e Autorização Segura com Tokens JWT.

✅ Controlo de Acesso Baseado em Perfis (@PreAuthorize).

✅ CRUD completo e paginado para a gestão de Processos.

✅ Criação de Etapas para a tramitação de processos.

✅ Upload de múltiplos ficheiros e download de todos os anexos como .zip.

✅ Geração automática de um PDF consolidado com o histórico de despachos.

✅ Captura e inserção de assinatura desenhada nos PDFs.

✅ Filtros de pesquisa avançados na listagem de processos ("Caixa de Entrada", "Enviados", "Todos").

✅ Tratamento global de exceções (@ControllerAdvice) para respostas de erro padronizadas.

⚙️ Como Executar o Projeto
Pré-requisitos
Java JDK 21 ou superior

Maven 3.6+

MySQL 8.0+

Um servidor web para PHP (XAMPP, WAMP, etc.)

Configuração do Backend (API Java)
Clone o repositório: git clone https://github.com/seu-usuario/seu-repositorio.git

Crie um banco de dados MySQL chamado dbsisimprensa.

Importe o ficheiro dbsisimprensa (12).sql para criar a estrutura das tabelas e popular com dados iniciais.

Na pasta src/main/resources/, crie o ficheiro application-local.properties e adicione as suas credenciais do banco de dados (este ficheiro é ignorado pelo .gitignore por segurança):

Properties

spring.datasource.url=jdbc:mysql://localhost:3306/dbsisimprensa
spring.datasource.username=root
spring.datasource.password=sua-senha-aqui

application.security.jwt.secret-key=EstaEhMinhaNovaChaveSecretaSuperLongaParaEvitarErros12345
Execute a aplicação através da sua IDE ou pelo terminal com o comando: mvn spring-boot:run.

Configuração do Frontend (PHP)
Copie os ficheiros PHP para a pasta do seu servidor web (ex: htdocs/sicom/).

Certifique-se de que os ficheiros de conexão com o banco de dados (ex: funcoes/conexao.php) estão com as credenciais corretas para o sistema PHP legado.

Aceda ao sistema através do seu navegador (ex: http://localhost/sicom/pages/PaginaInicial.php).

📚 Documentação da API
A API está documentada com Swagger (OpenAPI). Após iniciar o backend, a documentação interativa estará disponível em:
http://localhost:9090/swagger-ui.html
