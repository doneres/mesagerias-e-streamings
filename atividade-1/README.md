1- * O sistema de Notas Fiscais parou no mesmo instante? Não. O Módulo de Vendas termina sua execução muito mais rápido (simulando 200ms por venda), enquanto o Emissor de Notas continua trabalhando para processar o que ficou acumulado na fila (levando 1.5s por nota). +2 
*  Vantagem para o usuário: A maior vantagem é a baixa latência. O cliente recebe a confirmação da compra instantaneamente ("OK") e pode continuar navegando no site, sem precisar esperar o processo lento e instável da SEFAZ terminar para saber se o pedido foi aceito. 

2-* O que acontece com o produtor quando a fila enche? Quando a ArrayBlockingQueue atinge seu limite (neste caso, 2), o método fila.put(p) faz com que a thread do Módulo de Vendas trave (bloqueie). +2 
*  Impacto: Ele deixa de ser rápido e passa a trabalhar no ritmo do consumidor lento. Isso acontece porque não há mais espaço no "buffer" para armazenar novos pedidos, forçando o sistema de vendas a esperar que uma vaga seja liberada pelo emissor de notas.


3-
