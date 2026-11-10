# ADR 0004 — Alerta crítico assíncrono via SNS, com tratamento de falhas

**Situação:** aceita

## Contexto

Um feedback crítico precisa gerar um e-mail ao administrador. Enviar o e-mail dentro da função que recebe a avaliação daria a ela duas responsabilidades e faria o estudante esperar (e eventualmente receber erro) por causa de um serviço de e-mail.

Gravar no banco e publicar uma mensagem são duas operações em serviços diferentes, sem transação entre elas. É preciso decidir o que acontece quando só uma dá certo.

## Decisão

1. A função de recebimento grava a avaliação e publica uma mensagem em um tópico SNS; outra função, assinante do tópico, envia o e-mail pelo SES.
2. A gravação vem primeiro. Se a publicação falhar, a avaliação continua registrada, o estudante recebe `201` e a falha é registrada com um marcador em log. Um filtro de métrica conta esse marcador e um alarme avisa o administrador.
3. Se o envio do e-mail falhar, o Lambda repete duas vezes; persistindo, a mensagem vai para uma fila SQS (DLQ) com alarme.

## Alternativas consideradas

- **Responder `500` quando a publicação falha:** o estudante reenviaria um feedback que já foi salvo, gerando duplicidade.
- **Publicar antes de gravar:** poderia avisar o administrador de um feedback que não foi registrado.
- **DynamoDB Streams acionando a função de notificação:** elimina a escrita dupla, pois o alerta passa a derivar do que foi gravado. É a evolução natural; não foi adotada agora para manter o desenho mais simples de demonstrar e porque a combinação de novas tentativas do SDK com o alarme cobre a falha.
- **Assinatura de e-mail direto no SNS:** dispensaria a segunda função, mas sem controle sobre o formato da mensagem.

## Consequências

- Recebimento e notificação evoluem e falham de forma independente.
- Nenhum feedback aceito é perdido e nenhuma falha de alerta passa em silêncio.
- A entrega é *ao menos uma vez*: em casos raros o administrador pode receber o mesmo aviso duas vezes.
- Um alerta cuja publicação falhou não é reenviado sozinho; o administrador é avisado pelo alarme e encontra o identificador no log.
