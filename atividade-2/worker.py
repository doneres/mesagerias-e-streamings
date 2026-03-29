import time
import stomp
import json
import psycopg2

DB_CONN = "dbname='logistica' user='admin' host='localhost' password='admin'"

class LogisticaListener(stomp.ConnectionListener):
    def on_message(self, frame):
        dados = json.loads(frame.body)
        pedido_id = dados['pedido_id']
        
        print(f"[WORKER] Recebeu evento para roteirizar o pedido ID: {pedido_id}")
        
        # Simula um processamento pesado (ex: cálculo de rotas no Google Maps)
        time.sleep(5) 
        
        # Atualiza o status no PostgreSQL
        conn = psycopg2.connect(DB_CONN)
        cur = conn.cursor()
        cur.execute("UPDATE pedidos SET status = 'ENVIADO' WHERE id = %s", (pedido_id,))
        conn.commit()
        cur.close()
        conn.close()
        
        print(f"[WORKER] Pedido {pedido_id} processado com sucesso!")

# Conecta ao ActiveMQ
conn = stomp.Connection([('localhost', 62200)])
conn.set_listener('', LogisticaListener())
conn.connect('admin', 'admin', wait=True)

# Inscreve-se na fila
conn.subscribe(destination='/queue/logistica.entregas', id=1, ack='auto')

print("[WORKER] Aguardando novos pedidos...")
while True:
    time.sleep(1) # Mantém o script rodando
