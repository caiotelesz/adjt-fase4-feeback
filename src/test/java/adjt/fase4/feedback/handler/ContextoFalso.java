package adjt.fase4.feedback.handler;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/** Contexto do Lambda para testes: guarda as mensagens de log em memória. */
class ContextoFalso implements Context {

  final List<String> logs = new ArrayList<>();

  private final LambdaLogger logger = new LambdaLogger() {
    @Override
    public void log(String mensagem) {
      logs.add(mensagem);
    }

    @Override
    public void log(byte[] mensagem) {
      logs.add(new String(mensagem));
    }
  };

  @Override
  public LambdaLogger getLogger() {
    return logger;
  }

  @Override
  public String getAwsRequestId() {
    return "requisicao-de-teste";
  }

  @Override
  public String getLogGroupName() {
    return "grupo-de-teste";
  }

  @Override
  public String getLogStreamName() {
    return "fluxo-de-teste";
  }

  @Override
  public String getFunctionName() {
    return "funcao-de-teste";
  }

  @Override
  public String getFunctionVersion() {
    return "$LATEST";
  }

  @Override
  public String getInvokedFunctionArn() {
    return "arn:aws:lambda:us-east-2:000000000000:function:funcao-de-teste";
  }

  @Override
  public CognitoIdentity getIdentity() {
    return null;
  }

  @Override
  public ClientContext getClientContext() {
    return null;
  }

  @Override
  public int getRemainingTimeInMillis() {
    return 15_000;
  }

  @Override
  public int getMemoryLimitInMB() {
    return 512;
  }
}
