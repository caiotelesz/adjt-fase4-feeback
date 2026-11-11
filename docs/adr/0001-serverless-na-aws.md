# ADR 0001 — Serverless com AWS Lambda e SAM

**Situação:** aceita

## Contexto

O enunciado exige funções serverless em ambiente de nuvem, com deploy automatizado e créditos limitados. A carga é irregular: feedbacks chegam em rajadas e os outros dois fluxos são reações a eventos (mensagem e agendamento).

## Decisão

Usar o modelo FaaS na AWS: AWS Lambda (Java 21, arm64) para o código e serviços gerenciados para o restante (API Gateway, DynamoDB, SNS, SQS, SES, EventBridge). Toda a infraestrutura é declarada em um template AWS SAM.

## Alternativas consideradas

- **Contêineres (ECS/Fargate ou Kubernetes):** exigem um serviço sempre ligado e mais operação, sem ganho para três fluxos curtos e orientados a eventos.
- **Spring Boot ou Quarkus dentro do Lambda:** facilitam a injeção de dependências, mas aumentam o pacote e o *cold start*. Com três funções pequenas, a montagem manual das dependências no construtor do handler é suficiente.
- **Terraform ou CDK:** equivalentes para o objetivo. O SAM foi escolhido por ser a extensão do CloudFormation voltada a serverless, com menos código para o mesmo resultado.

## Consequências

- Custo zero com a plataforma parada e escala automática.
- *Cold start* do Java na primeira chamada. Mitigado com cliente HTTP leve, 512 MB de memória e arm64. O SnapStart não está disponível para funções em imagem de contêiner (ver ADR 0006); se a partida a frio se tornar um problema, a alternativa é a simultaneidade provisionada.
- Acoplamento ao provedor, contido nos pacotes `handler` e `infrastructure` (ver ADR 0002).
