from flask import Flask, request, jsonify, render_template
import psycopg2
import stomp
import json

app = Flask(__name__)

# Configuração do PostgreSQL
DB_CONN = "dbname='logistica' user='admin' host='localhost' password='admin'"

def init_db():
    conn = psycopg2.connect(DB_CONN)
    cur = conn.cursor()
    cur.execute("""
        CREATE TABLE IF NOT EXISTS pedidos (
            id SERIAL PRIMARY KEY,
            cliente VARCHAR(100),
            destino VARCHAR(100),
            status VARCHAR(20)
        )
    """)
    conn.commit()
    cur.close()
    conn.close()

# Configuração do Produtor ActiveMQ (STOMP)
def enviar_para_broker(pedido_id):
    conn = stomp.Connection([('localhost', 62200)])
    conn.connect('admin', 'admin', wait=True)
    
    mensagem = json.dumps({"pedido_id": pedido_id})
    # Envia a mensagem para a fila 'logistica.entregas'
    conn.send(body=mensagem, destination='/queue/logistica.entregas')
    conn.disconnect()

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/pedidos', methods=['POST'])
def criar_pedido():
    dados = request.json
    cliente = dados.get('cliente')
    destino = dados.get('destino')
    
    # 1. Salva no Postgres com status PENDENTE (Síncrono)
    conn = psycopg2.connect(DB_CONN)
    cur = conn.cursor()
    cur.execute(
        "INSERT INTO pedidos (cliente, destino, status) VALUES (%s, %s, %s) RETURNING id",
        (cliente, destino, 'PENDENTE')
    )
    pedido_id = cur.fetchone()[0]
    conn.commit()
    cur.close()
    conn.close()
    
    # 2. Emite o evento para o ActiveMQ (Assíncrono)
    enviar_para_broker(pedido_id)
    
    # 3. Libera a tela do usuário imediatamente
    return jsonify({"mensagem": "Pedido recebido!", "id": pedido_id}), 201

@app.route('/api/pedidos', methods=['GET'])
def listar_pedidos():
    conn = psycopg2.connect(DB_CONN)
    cur = conn.cursor()
    cur.execute("SELECT id, cliente, destino, status FROM pedidos ORDER BY id DESC")
    pedidos = [{"id": linha[0], "cliente": linha[1], "destino": linha[2], "status": linha[3]} for linha in cur.fetchall()]
    cur.close()
    conn.close()
    return jsonify(pedidos)

if __name__ == '__main__':
    init_db()
    app.run(debug=True, port=5000)
