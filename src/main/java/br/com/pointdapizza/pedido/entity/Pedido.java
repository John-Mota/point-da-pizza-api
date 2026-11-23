package br.com.pointdapizza.pedido.entity;

import br.com.pointdapizza.auth.entity.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "pedido")
public class Pedido extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    public UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "numero_sequencial", nullable = false, insertable = false, updatable = false)
    public Long numeroSequencial;

    @Generated(event = EventType.INSERT)
    @Column(name = "numero", length = 20, insertable = false, updatable = false)
    public String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    public String idempotencyKey;

    @Column(name = "cliente_nome", nullable = false, length = 150)
    public String clienteNome;

    @Column(name = "cliente_telefone", nullable = false, length = 25)
    public String clienteTelefone;

    @Column(name = "tipo_entrega", nullable = false, length = 20)
    public String tipoEntrega;

    @Column(name = "endereco", length = 300)
    public String endereco;

    @Column(name = "complemento", length = 150)
    public String complemento;

    @Column(name = "forma_pagamento", nullable = false, length = 60)
    public String formaPagamento;

    @Column(name = "troco_para", precision = 10, scale = 2)
    public BigDecimal trocoPara;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    public BigDecimal total;

    @Column(name = "status", nullable = false, length = 30)
    public String status = "recebido";

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    public List<PedidoItem> itens = new ArrayList<>();

    @PrePersist
    void prePersist() {
        OffsetDateTime agora = OffsetDateTime.now();
        if (this.criadoEm == null) {
            this.criadoEm = agora;
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "recebido";
        }
        this.atualizadoEm = agora;
    }

    @PreUpdate
    void preUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }
}
