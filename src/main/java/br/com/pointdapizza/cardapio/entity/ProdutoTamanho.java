package br.com.pointdapizza.cardapio.entity;

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
@Table(name = "produto_tamanho")
public class ProdutoTamanho extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    public Produto produto;

    @Column(name = "codigo", nullable = false, length = 32)
    public String codigo;

    @Column(name = "nome", nullable = false, length = 80)
    public String nome;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    public BigDecimal preco;

    @Column(name = "ordem", nullable = false)
    public Short ordem = 1;
}
