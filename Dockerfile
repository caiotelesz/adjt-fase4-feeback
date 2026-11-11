# Imagem das funções Lambda da plataforma de feedback.
#
# Parte da imagem oficial da AWS para Java 21, que já traz o runtime do Lambda.
# O pacote (target/feedback-platform.jar) é gerado e testado antes, pelo Maven;
# aqui ele é apenas copiado, de modo que a imagem contém exatamente o que passou
# nos testes.
#
# As quatro funções usam a mesma imagem. O handler de cada uma é definido no
# template.yaml (ImageConfig.Command); o CMD abaixo é só o padrão.
FROM public.ecr.aws/lambda/java:21

COPY target/feedback-platform.jar ${LAMBDA_TASK_ROOT}/lib/

CMD ["adjt.fase4.feedback.handler.AvaliacaoHandler::handleRequest"]
