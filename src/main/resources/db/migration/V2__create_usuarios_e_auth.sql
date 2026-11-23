CREATE TABLE usuario (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nome VARCHAR(150) NOT NULL,
  telefone VARCHAR(20),
  telefone_whatsapp BOOLEAN NOT NULL DEFAULT TRUE,
  email VARCHAR(180),
  endereco VARCHAR(300),
  complemento VARCHAR(150),
  senha_hash VARCHAR(255) NOT NULL,
  foto_url VARCHAR(500),
  role VARCHAR(30) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'PRESTADOR', 'ADMIN')),
  cadastrado_por_admin_id UUID REFERENCES usuario(id) ON DELETE RESTRICT,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CHECK (email IS NOT NULL OR telefone IS NOT NULL),
  CHECK (
    (role = 'PRESTADOR' AND cadastrado_por_admin_id IS NOT NULL)
    OR (role <> 'PRESTADOR' AND cadastrado_por_admin_id IS NULL)
  ),
  CHECK (cadastrado_por_admin_id IS NULL OR cadastrado_por_admin_id <> id)
);

CREATE UNIQUE INDEX uq_usuario_email
  ON usuario (LOWER(email))
  WHERE email IS NOT NULL;

CREATE UNIQUE INDEX uq_usuario_telefone
  ON usuario (telefone)
  WHERE telefone IS NOT NULL;

CREATE INDEX idx_usuario_role
  ON usuario (role);

CREATE INDEX idx_usuario_cadastrado_por_admin_id
  ON usuario (cadastrado_por_admin_id)
  WHERE cadastrado_por_admin_id IS NOT NULL;

CREATE OR REPLACE FUNCTION fn_validar_admin_cadastrador_prestador()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.role = 'PRESTADOR' THEN
    IF NEW.cadastrado_por_admin_id IS NULL THEN
      RAISE EXCEPTION 'Usuário com role PRESTADOR deve possuir cadastrado_por_admin_id.';
    END IF;

    IF NOT EXISTS (
      SELECT 1
      FROM usuario
      WHERE id = NEW.cadastrado_por_admin_id
        AND role = 'ADMIN'
    ) THEN
      RAISE EXCEPTION 'O usuário informado em cadastrado_por_admin_id deve possuir role ADMIN.';
    END IF;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_admin_cadastrador_prestador
  BEFORE INSERT OR UPDATE OF role, cadastrado_por_admin_id ON usuario
  FOR EACH ROW
  EXECUTE FUNCTION fn_validar_admin_cadastrador_prestador();
