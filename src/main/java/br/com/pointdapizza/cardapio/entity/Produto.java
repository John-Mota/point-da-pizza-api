package br.com.pointdapizza.cardapio.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Entity
@Table(name = "produto")
public class Produto extends PanacheEntityBase {

    @Id
    @Column(name = "id", length = 64)
    public String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    public Categoria categoria;

    @Column(name = "nome", nullable = false, length = 160)
    public String nome;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    public String descricao;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    public BigDecimal preco;

    @Column(name = "preco_original", precision = 10, scale = 2)
    public BigDecimal precoOriginal;

    @Column(name = "imagem_url", length = 500)
    public String imagemUrl;

    @Column(name = "disponivel", nullable = false)
    public boolean disponivel = true;

    @Column(name = "destaque", nullable = false)
    public boolean destaque = false;

    @Column(name = "ordem_destaque")
    public Integer ordemDestaque;

    @Column(name = "selo_destaque", length = 60)
    public String seloDestaque;

    @Column(name = "ordem_exibicao", nullable = false)
    public Integer ordemExibicao = 1;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    public List<ProdutoTamanho> tamanhos = new ArrayList<>();

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    public List<ProdutoBorda> bordas = new ArrayList<>();

    @PrePersist
    void prePersist() {
        OffsetDateTime agora = OffsetDateTime.now();
        if (this.criadoEm == null) {
            this.criadoEm = agora;
        }
        this.atualizadoEm = agora;
    }

    @PreUpdate
    void preUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }
}
