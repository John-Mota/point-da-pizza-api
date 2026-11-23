CREATE TABLE loja (
  id BIGSERIAL PRIMARY KEY,
  nome VARCHAR(120) NOT NULL,
  slogan VARCHAR(255) NOT NULL,
  logo_url VARCHAR(500),
  capa_url VARCHAR(500),
  aberta BOOLEAN NOT NULL DEFAULT TRUE,
  endereco VARCHAR(300) NOT NULL,
  telefone VARCHAR(20),
  atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE loja_horario (
  id BIGSERIAL PRIMARY KEY,
  loja_id BIGINT NOT NULL REFERENCES loja(id) ON DELETE CASCADE,
  dia SMALLINT NOT NULL CHECK (dia BETWEEN 0 AND 6),
  abre CHAR(5) NOT NULL CHECK (abre ~ '^\d{2}:\d{2}$'),
  fecha CHAR(5) NOT NULL CHECK (fecha ~ '^\d{2}:\d{2}$'),
  UNIQUE (loja_id, dia)
);

CREATE TABLE loja_forma_pagamento (
  id BIGSERIAL PRIMARY KEY,
  loja_id BIGINT NOT NULL REFERENCES loja(id) ON DELETE CASCADE,
  descricao VARCHAR(80) NOT NULL,
  ordem SMALLINT NOT NULL DEFAULT 1,
  UNIQUE (loja_id, descricao)
);

CREATE TABLE categoria (
  id VARCHAR(64) PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  ordem INTEGER NOT NULL,
  imagem_url VARCHAR(500),
  ativa BOOLEAN NOT NULL DEFAULT TRUE,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE produto (
  id VARCHAR(64) PRIMARY KEY,
  categoria_id VARCHAR(64) NOT NULL REFERENCES categoria(id) ON DELETE RESTRICT,
  nome VARCHAR(160) NOT NULL,
  descricao TEXT NOT NULL,
  preco NUMERIC(10, 2) NOT NULL CHECK (preco >= 0),
  preco_original NUMERIC(10, 2) CHECK (preco_original IS NULL OR preco_original >= preco),
  imagem_url VARCHAR(500),
  disponivel BOOLEAN NOT NULL DEFAULT TRUE,
  destaque BOOLEAN NOT NULL DEFAULT FALSE,
  ordem_destaque INTEGER,
  selo_destaque VARCHAR(60),
  ordem_exibicao INTEGER NOT NULL DEFAULT 1,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE produto_tamanho (
  id BIGSERIAL PRIMARY KEY,
  produto_id VARCHAR(64) NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
  codigo VARCHAR(32) NOT NULL,
  nome VARCHAR(80) NOT NULL,
  preco NUMERIC(10, 2) NOT NULL CHECK (preco >= 0),
  ordem SMALLINT NOT NULL DEFAULT 1,
  UNIQUE (produto_id, codigo)
);

CREATE TABLE produto_borda (
  id BIGSERIAL PRIMARY KEY,
  produto_id VARCHAR(64) NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
  codigo VARCHAR(32) NOT NULL,
  nome VARCHAR(80) NOT NULL,
  acrescimo NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (acrescimo >= 0),
  ordem SMALLINT NOT NULL DEFAULT 1,
  UNIQUE (produto_id, codigo)
);

CREATE INDEX idx_loja_horario_loja_id ON loja_horario (loja_id);
CREATE INDEX idx_loja_forma_pagamento_loja_id ON loja_forma_pagamento (loja_id);
CREATE INDEX idx_produto_categoria_id ON produto (categoria_id);
CREATE INDEX idx_produto_tamanho_produto_id ON produto_tamanho (produto_id);
CREATE INDEX idx_produto_borda_produto_id ON produto_borda (produto_id);
