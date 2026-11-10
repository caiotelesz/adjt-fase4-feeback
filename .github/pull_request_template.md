Descreva o problema e o comportamento resultante.

Validação realizada e eventuais limites:

- [ ] `mvn clean verify` passou localmente (testes, regras de arquitetura e cobertura mínima).
- [ ] Nenhum segredo, e-mail pessoal ou identificador de conta foi incluído.
- [ ] Mudanças no `template.yaml` foram revisadas quanto a permissões (menor privilégio) e custo.
- [ ] README e ADRs foram atualizados, quando a mudança altera comportamento, API ou arquitetura.

O merge na `main` publica automaticamente na AWS e registra uma nova release. Peça a revisão de outra pessoa do grupo antes do merge.
