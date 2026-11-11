# ADR 0006 — Funções publicadas como imagem de contêiner

**Situação:** aceita

## Contexto

O AWS Lambda aceita o código de uma função em dois formatos: um arquivo Zip (no Java, o `.jar`) ou uma imagem de contêiner guardada no Amazon ECR. A primeira versão do projeto usava Zip.

Nos dois formatos a publicação é automática, feita pelo pipeline a cada merge na `main`. A diferença está no artefato que representa uma versão e em onde ele fica.

## Decisão

Publicar as quatro funções a partir de **uma única imagem de contêiner**, construída pelo pipeline e guardada no Amazon ECR.

- A imagem parte da imagem oficial da AWS para Java 21 e recebe o pacote já compilado e testado pelo Maven; nada é compilado dentro do contêiner.
- As funções diferem apenas pelo handler, definido no template (`ImageConfig.Command`).
- Cada publicação gera uma imagem com a versão como tag (`feedback-platform:1.0.1`); o deploy a referencia pelo digest, que identifica o conteúdo e não pode ser reaproveitado.
- O repositório verifica vulnerabilidades a cada envio e mantém as dez imagens mais recentes.

## Alternativas consideradas

- **Manter o Zip:** mais simples, sem Docker nem ECR, e com partida a frio mais rápida. Foi a escolha inicial e continua adequada para funções pequenas.
- **Uma imagem por função:** permitiria imagens menores, mas multiplicaria builds e repositórios para um código que é o mesmo.
- **Compilar dentro da imagem (build em múltiplos estágios):** deixaria a imagem independente do Maven local, porém o que roda deixaria de ser exatamente o pacote que passou nos testes do pipeline.

## Consequências

- Cada versão publicada tem um artefato identificável pela tag e fixado pelo digest, que é o que o deploy referencia, o mesmo modelo usado em ambientes de contêineres; voltar a uma versão é apontar para a imagem anterior.
- A imagem pode ser executada localmente com o emulador do Lambda embutido na imagem base, o que permite testar a função fora da AWS.
- A verificação de vulnerabilidades passa a cobrir também a imagem base, e não só as bibliotecas Java.
- A partida a frio fica mais lenta do que com Zip, pois a imagem é maior (cerca de 125 MB contra 14 MB).
- Há custo de armazenamento no ECR, pequeno e limitado pela política de retenção.
- O pipeline e o papel de deploy ganham uma etapa e permissões a mais (construir, autenticar e enviar a imagem).
- Passar de Zip para imagem obriga o CloudFormation a substituir as funções uma vez; os dados e o endereço da API são preservados.
