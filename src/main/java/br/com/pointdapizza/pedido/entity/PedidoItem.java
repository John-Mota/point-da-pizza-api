package br.com.pointdapizza.pedido.entity;

import br.com.pointdapizza.cardapio.entity.Produto;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido_item")
public class PedidoItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    public Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    public Produto produto;

    @Column(name = "nome", nullable = false, length = 160)
    public String nome;

    @Column(name = "quantidade", nullable = false)
    public Integer quantidade;

    @Column(name = "tamanho_id", length = 32)
    public String tamanhoId;

    @Column(name = "tamanho_nome", length = 80)
    public String tamanhoNome;

    @Column(name = "borda_id", length = 32)
    public String bordaId;

    @Column(name = "borda_nome", length = 80)
    public String bordaNome;

    @Column(name = "observacao", length = 140)
    public String observacao;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    public BigDecimal precoUnitario;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    public BigDecimal total;

    @Column(name = "ordem", nullable = false)
    public Short ordem = 1;
}
