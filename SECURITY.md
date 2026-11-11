# Relatar um problema de segurança

Relate suspeitas diretamente a quem mantém o repositório, por um canal privado. Não publique em issues ou comentários detalhes exploráveis, chaves de API, e-mails, identificadores de conta AWS nem o conteúdo de feedbacks.

Use dados fictícios nas reproduções. Não execute testes de carga nem testes destrutivos contra o ambiente publicado sem autorização: a API tem cota diária, e um feedback com nota baixa dispara e-mail aos administradores.

## Como a aplicação é protegida

- **Acesso à API:** chave (`x-api-key`) com plano de uso e limite de requisições. A única rota pública é `GET /versao`, que não expõe dados.
- **Menor privilégio:** cada função Lambda tem o próprio papel IAM, restrito às ações e aos recursos que utiliza.
- **Dados:** DynamoDB criptografado em repouso, com recuperação a um ponto no tempo; o feedback é anônimo e a descrição não é escrita em log.
- **Deploy:** o pipeline autentica na AWS por OIDC, sem chave de acesso guardada, e o papel de deploy só é concedido a execuções na branch `main`. Proteger a `main` (pull request e verificações obrigatórios) é configuração do repositório e cabe a quem o administra.
- **Dependências:** as versões das bibliotecas e das ações do pipeline são fixadas no repositório e atualizadas por pull request, com todos os testes.

## Ao corrigir uma falha

Antes de publicar a correção, registre um teste que demonstre a falha, execute `mvn clean verify` e abra um pull request. A publicação acontece pelo pipeline, no merge para a `main`.

Nenhum resultado de teste prova a ausência de falhas. Este é um projeto acadêmico; a chave de API identifica a aplicação cliente, não o estudante.
