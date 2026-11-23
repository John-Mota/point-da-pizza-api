package br.com.pointdapizza;

import br.com.pointdapizza.entity.Categoria;
import br.com.pointdapizza.entity.Loja;
import br.com.pointdapizza.entity.LojaFormaPagamento;
import br.com.pointdapizza.entity.LojaHorario;
import br.com.pointdapizza.entity.Produto;
import br.com.pointdapizza.entity.ProdutoBorda;
import br.com.pointdapizza.entity.ProdutoTamanho;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class TestSeedHelper {

    @Transactional
    public void garantirDadosBase() {
        if (Loja.count() == 0) {
            Loja loja = new Loja();
            loja.nome = "Point da Pizza";
            loja.slogan = "Sabor que te leva ao ponto!";
            loja.logoUrl = "/img/logo.png";
            loja.capaUrl = "/img/categoria-pizzas.jpg";
            loja.aberta = true;
            loja.endereco = "Av. Principal, 450 - Centro, Fortaleza/CE";
            loja.telefone = "85988776655";

            for (short dia : new short[] { 0, 2, 3, 4, 5, 6 }) {
                LojaHorario horario = new LojaHorario();
                horario.loja = loja;
                horario.dia = dia;
                horario.abre = "18:00";
                horario.fecha = "23:30";
                loja.horarios.add(horario);
            }

            List<String> pagamentos = List.of("Pix", "Cartão de crédito", "Cartão de débito", "Dinheiro");
            for (int i = 0; i < pagamentos.size(); i++) {
                LojaFormaPagamento fp = new LojaFormaPagamento();
                fp.loja = loja;
                fp.descricao = pagamentos.get(i);
                fp.ordem = (short) (i + 1);
                loja.formasPagamento.add(fp);
            }

            loja.persist();
        }

        if (Categoria.count() == 0) {
            Categoria pizzas = criarCategoria("pizzas", "Pizzas", 1, "/img/categoria-pizzas.jpg");
            Categoria pasteis = criarCategoria("pasteis", "Pastéis", 2, "/img/categoria-pasteis.jpg");
            criarCategoria("pasteis-especiais", "Pastéis Especiais", 3, null);
            Categoria calzones = criarCategoria("calzones", "Calzones", 4, "/img/calzone-assado.jpg");
            criarCategoria("porcoes", "Porções", 5, null);
            Categoria bebidas = criarCategoria("bebidas", "Bebidas", 6, "/img/categoria-bebidas.jpg");
            Categoria combos = criarCategoria("combos", "Combos", 7, null);
            Categoria lasanhas = criarCategoria("lasanhas", "Lasanhas", 8, "/img/lasanha-bolonhesa.jpg");

            // Combos
            criarProduto("prod-promo-1", combos, "2 Pizzas G com borda de requeijão",
                "Escolha até 2 sabores por pizza grande, ambas com borda recheada de requeijão cremoso.",
                "62.00", "79.80", "/img/promo-2-pizzas-g-pizza-p-chocolate-coca-2l.jpg", true, 1, "Super Oferta", 1);
            criarProduto("prod-promo-2", combos, "1 Pizza G com borda + refrigerante 1L",
                "1 pizza grande tradicional com borda de requeijão + 1 refrigerante de 1 litro gelado.",
                "36.90", "46.90", "/img/promo-1-pizza-g-borda-refri-1l.jpg", true, 2, "Mais Pedido", 2);
            criarProduto("prod-promo-3", combos, "2 Calzones + refrigerante 1L",
                "2 calzones assados na hora bem recheados + 1 refrigerante de 1 litro.",
                "41.90", "51.00", "/img/calzone-assado.jpg", true, 3, "Combo Especial", 3);
            criarProduto("prod-promo-4", combos, "Combo Família 2 Pizzas G + Pizza P Doce + Refri 2L",
                "2 pizzas grandes com borda de requeijão + 1 pizza pequena de chocolate + 1 refrigerante de 2 litros.",
                "89.90", "109.90", "/img/promo-pizza-g-pizza-p-chocolate-refri-1l.jpg", true, 4, "Combo Família", 4);

            // Pizzas
            Produto trad1 = criarProduto("prod-trad-1", pizzas, "Pizza Calabresa Acebolada",
                "Molho artesanal, mussarela derretida, calabresa fatiada, cebola roxa e orégano.",
                "32.90", null, "/img/pizza-calabresa-pepperoni.jpg", true, 5, "Favorita da Casa", 1);
            adicionarOpcoesPizza(trad1);

            Produto trad2 = criarProduto("prod-trad-2", pizzas, "Pizza Mussarela Tradicional",
                "Molho artesanal, camada dupla de mussarela derretida, rodelas de tomate e orégano.",
                "34.90", null, "/img/pizza-mussarela.jpg", false, null, null, 2);
            adicionarOpcoesPizza(trad2);

            criarProduto("prod-trad-3", pizzas, "Pizza Marguerita Especial",
                "Molho artesanal, mussarela, tomates selecionados, folhas de manjericão fresco e azeite.",
                "35.90", null, "/img/pizza-tomate-manjericao.jpg", false, null, null, 3);
            criarProduto("prod-trad-4", pizzas, "Pizza Quatro Queijos da Casa",
                "Molho artesanal, mussarela especial, requeijão cremoso, parmesão gratinado e orégano.",
                "37.90", null, "/img/pizza-mussarela-especial.jpg", false, null, null, 4);
            criarProduto("prod-doce-1", pizzas, "Pizza Brigadeiro Tradicional",
                "Cobertura generosa de chocolate ao leite cremoso com granulado crocante e cerejas.",
                "33.90", null, "/img/pizza-chocolate.jpg", false, null, null, 5);
            criarProduto("prod-doce-2", pizzas, "Pizza Chocolate com Morango",
                "Chocolate ao leite derretido coberto com morangos frescos selecionados e leite condensado.",
                "36.90", null, "/img/pizza-chocolate.jpg", false, null, null, 6);
            criarProduto("prod-doce-3", pizzas, "Pizza Romeu e Julieta",
                "Mussarela especial combinada com goiabada cremosa derretida no forno.",
                "32.90", null, "/img/pizza-mussarela-especial.jpg", false, null, null, 7);

            // Calzones, Pasteis e Lasanhas
            criarProduto("prod-calz-1", calzones, "Calzone Frango com Requeijão",
                "Massa fechada assada no forno, recheada com frango desfiado, mussarela, milho e requeijão.",
                "21.90", null, "/img/calzone-assado.jpg", false, null, null, 1);
            criarProduto("prod-calz-2", calzones, "Calzone Calabresa com Queijo",
                "Massa dourada recheada com calabresa defumada, mussarela, cebola e orégano.",
                "21.90", null, "/img/calzone-assado.jpg", false, null, null, 2);
            criarProduto("prod-calz-3", pasteis, "Pastel de Forno Carne de Sol",
                "Pastel grande assado com recheio cremoso de carne de sol e queijo coalho.",
                "16.90", null, "/img/pastel-forno.jpg", false, null, null, 1);
            criarProduto("prod-calz-4", lasanhas, "Lasanha à Bolonhesa da Casa",
                "Lasanha artesanal bem recheada com molho à bolonhesa, presunto e mussarela gratinada.",
                "24.90", null, "/img/lasanha-bolonhesa.jpg", false, null, null, 1);

            // Bebidas
            criarProduto("prod-beb-1", bebidas, "Refrigerante 1 Litro",
                "Coca-Cola, Guaraná Antarctica, Pepsi ou São Geraldo de 1 litro bem gelado.",
                "8.00", null, "/img/refri-coca-cola-1l.jpg", false, null, null, 1);
            criarProduto("prod-beb-2", bebidas, "Refrigerante 2 Litros",
                "Coca-Cola, Guaraná Antarctica, Sukita ou Cajuína São Geraldo 2 litros.",
                "13.00", null, "/img/refri-coca-cola-2l.jpg", false, null, null, 2);
            criarProduto("prod-beb-3", bebidas, "Refrigerante Lata 350ml",
                "Coca-Cola original ou Guaraná Antarctica em lata 350ml.",
                "5.50", null, "/img/refri-coca-cola-lata-350ml.jpg", false, null, null, 3);
            criarProduto("prod-beb-4", bebidas, "Suco Natural 500ml",
                "Suco gelado de acerola, cajá, goiaba, graviola, manga ou maracujá feito na hora (500ml).",
                "7.50", null, "/img/suco-maracuja.jpg", false, null, null, 4);
        }
    }

    private Categoria criarCategoria(String id, String nome, int ordem, String imagemUrl) {
        Categoria c = new Categoria();
        c.id = id;
        c.nome = nome;
        c.ordem = ordem;
        c.imagemUrl = imagemUrl;
        c.ativa = true;
        c.persist();
        return c;
    }

    private Produto criarProduto(
        String id,
        Categoria categoria,
        String nome,
        String descricao,
        String preco,
        String precoOriginal,
        String imagemUrl,
        boolean destaque,
        Integer ordemDestaque,
        String seloDestaque,
        int ordemExibicao
    ) {
        Produto p = new Produto();
        p.id = id;
        p.categoria = categoria;
        p.nome = nome;
        p.descricao = descricao;
        p.preco = new BigDecimal(preco);
        p.precoOriginal = precoOriginal != null ? new BigDecimal(precoOriginal) : null;
        p.imagemUrl = imagemUrl;
        p.disponivel = true;
        p.destaque = destaque;
        p.ordemDestaque = ordemDestaque;
        p.seloDestaque = seloDestaque;
        p.ordemExibicao = ordemExibicao;
        p.persist();
        return p;
    }

    private void adicionarOpcoesPizza(Produto produto) {
        ProdutoTamanho p = new ProdutoTamanho();
        p.produto = produto;
        p.codigo = "P";
        p.nome = "Pequena (P)";
        p.preco = new BigDecimal("29.90");
        p.ordem = 1;
        p.persist();

        ProdutoTamanho g = new ProdutoTamanho();
        g.produto = produto;
        g.codigo = "G";
        g.nome = "Grande (G)";
        g.preco = new BigDecimal("34.90");
        g.ordem = 2;
        g.persist();

        ProdutoBorda b = new ProdutoBorda();
        b.produto = produto;
        b.codigo = "requeijao";
        b.nome = "Requeijão";
        b.acrescimo = new BigDecimal("0.00");
        b.ordem = 1;
        b.persist();
    }
}
