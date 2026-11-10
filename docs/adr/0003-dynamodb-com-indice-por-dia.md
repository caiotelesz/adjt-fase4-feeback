# ADR 0003 — DynamoDB com índice por dia para o relatório

**Situação:** aceita

## Contexto

Há dois padrões de acesso: gravar uma avaliação (chave: `id`) e ler todas as avaliações de um período (relatório semanal). O segundo não é atendido pela chave primária.

## Decisão

Usar DynamoDB sob demanda, com chave de partição `id` e um índice secundário global `por-dia`: partição `diaEnvio` (data em UTC) e ordenação `dataEnvio`. O relatório faz uma consulta (*Query*) por dia do período, oito para os últimos sete dias. As datas são gravadas em UTC com tamanho fixo, para que a ordem alfabética coincida com a cronológica.

## Alternativas consideradas

- **Varredura completa (*Scan*) com filtro:** mais simples, mas lê a tabela inteira a cada relatório; custo e tempo crescem com todo o histórico, não com a semana.
- **Uma única partição com todas as avaliações, ordenada por data:** uma só consulta, porém concentra toda a escrita em uma partição.
- **Banco relacional (RDS/Aurora):** consulta por período trivial, mas exige instância ligada, rede privada e gestão de conexões no Lambda, desproporcionais ao problema.

## Consequências

- O custo do relatório depende do volume da semana.
- O papel da função de relatório tem `dynamodb:Query` apenas no índice.
- O índice duplica o armazenamento dos itens (projeção completa), aceitável para itens pequenos.
- Leituras em índice global são eventualmente consistentes: uma avaliação gravada segundos antes do relatório pode ficar para o seguinte.
