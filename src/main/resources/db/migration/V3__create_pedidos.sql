CREATE SEQUENCE pedido_numero_seq START WITH 101 INCREMENT BY 1;

CREATE TABLE pedido (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  numero_sequencial BIGINT NOT NULL DEFAULT nextval('pedido_numero_seq'),
  numero VARCHAR(20) GENERATED ALWAYS AS ('#' || numero_sequencial::TEXT) STORED,
  usuario_id UUID REFERENCES usuario(id) ON DELETE SET NULL,
  idempotency_key VARCHAR(100) NOT NULL UNIQUE,
  cliente_nome VARCHAR(150) NOT NULL,
  cliente_telefone VARCHAR(25) NOT NULL,
  tipo_entrega VARCHAR(20) NOT NULL CHECK (tipo_entrega IN ('entrega', 'retirada')),
  endereco VARCHAR(300),
  complemento VARCHAR(150),
  forma_pagamento VARCHAR(60) NOT NULL,
  troco_para NUMERIC(10, 2) CHECK (troco_para IS NULL OR troco_para > 0),
  total NUMERIC(10, 2) NOT NULL CHECK (total >= 0),
  status VARCHAR(30) NOT NULL DEFAULT 'recebido' CHECK (status IN ('recebido', 'em-producao', 'a-caminho', 'entregue', 'cancelado')),
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE pedido_item (
  id BIGSERIAL PRIMARY KEY,
  pedido_id UUID NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
  produto_id VARCHAR(64) NOT NULL REFERENCES produto(id) ON DELETE RESTRICT,
  nome VARCHAR(160) NOT NULL,
  quantidade INTEGER NOT NULL CHECK (quantidade >= 1),
  tamanho_id VARCHAR(32),
  tamanho_nome VARCHAR(80),
  borda_id VARCHAR(32),
  borda_nome VARCHAR(80),
  observacao VARCHAR(140),
  preco_unitario NUMERIC(10, 2) NOT NULL CHECK (preco_unitario >= 0),
  total NUMERIC(10, 2) NOT NULL CHECK (total >= 0),
  ordem SMALLINT NOT NULL DEFAULT 1
);

CREATE INDEX idx_pedido_usuario_id ON pedido (usuario_id);
CREATE INDEX idx_pedido_criado_em ON pedido (criado_em DESC);
CREATE INDEX idx_pedido_item_pedido_id ON pedido_item (pedido_id);
CREATE INDEX idx_pedido_item_produto_id ON pedido_item (produto_id);
