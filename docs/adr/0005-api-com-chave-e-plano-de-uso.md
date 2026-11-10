# ADR 0005 — API protegida por chave e plano de uso

**Situação:** aceita

## Contexto

O enunciado pede governança de acesso. O endpoint recebe feedbacks anônimos de estudantes a partir da plataforma de cursos; uma API totalmente aberta pode ser abusada, gerando custo e inundando os administradores de e-mails.

## Decisão

Exigir chave de API (`x-api-key`) no API Gateway, associada a um plano de uso com limite de 10 requisições por segundo, rajada de 20 e cota de 5000 por dia. A chave identifica a aplicação cliente (a plataforma de cursos), não o estudante.

## Alternativas consideradas

- **API aberta, apenas com limite de requisições:** nenhum controle sobre quem chama.
- **Amazon Cognito ou autorizador JWT:** identifica o estudante e é o caminho para um produto real. Exige cadastro e login, fora do escopo do enunciado, e conflita com a opção por feedback anônimo.
- **Autorização IAM (SigV4):** adequada entre serviços, pouco prática para um cliente web.

## Consequências

- Controle e revogação de acesso por cliente, com cota diária.
- Uma chave de API não é um mecanismo forte de autenticação: se vazar, deve ser trocada. O limite de requisições e a cota reduzem o dano.
- Quem testa a API precisa informar o cabeçalho `x-api-key`.
