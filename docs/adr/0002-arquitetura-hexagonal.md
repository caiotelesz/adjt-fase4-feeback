# ADR 0002 — Arquitetura hexagonal dentro das funções

**Situação:** aceita

## Contexto

É comum, em projetos serverless, escrever a regra de negócio dentro do handler, misturada com o SDK da AWS. Isso dificulta os testes (que passam a depender de serviços reais ou de simulações pesadas) e espalha a regra por classes de infraestrutura. O enunciado avalia a separação de responsabilidades.

## Decisão

Organizar o código em portas e adaptadores, com dependências apontando para dentro:

- `domain`: regras de negócio, apenas Java;
- `application`: casos de uso e as portas (interfaces) de que precisam;
- `infrastructure`: adaptadores de saída (DynamoDB, SNS, SES);
- `handler`: adaptadores de entrada (as funções Lambda), que também montam as dependências.

As regras de dependência são verificadas por testes de arquitetura (ArchUnit).

## Alternativas consideradas

- **Lógica no handler:** menos classes, mas regra de negócio e SDK no mesmo lugar e testes dependentes da AWS.
- **Um projeto Maven por função:** isola o pacote de cada função, mas triplica a configuração de build. Como as três compartilham o mesmo domínio, optou-se por um único módulo com um handler por função. O custo é um pacote único com os três SDKs.

## Consequências

- 87 testes rodam sem rede e sem credenciais; os casos de uso são testados com dublês simples das portas.
- Trocar um serviço (por exemplo, o provedor de e-mail) altera apenas um adaptador.
- Mais classes do que a versão ingênua, o que se justifica pela testabilidade e pela clareza das responsabilidades.
