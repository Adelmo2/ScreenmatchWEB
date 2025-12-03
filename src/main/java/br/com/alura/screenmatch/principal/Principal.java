package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "http://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";      //key alura
    private List<DadosSerie> dadosSeries = new ArrayList<>();

    //Manter a linha abaixo comentada.
    //@Autowired
    private SerieRepository repositorio;

    private List<Serie> series = new ArrayList<>();

    private Optional<Serie> serieBusca;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                    1  - Buscar e cadastrar Séries 
                    2  - Buscar e caastrar Episódios
                    3  - Listar Séries Buscadas
                    4  - Buscar séries por título
                    5  - Buscar séries por ator
                    6  - Buscar Top 5 Series
                    7  - Buscar Séries por categoria
                    8  - Filtrar séries
                    9  - Busca episódio por trecho
                    10 - Top 5 Episoodios por serie.
                    11 - Buscar episódios a partir de uma data
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    tOpEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void  pausaTela() {
        System.out.println("\nPressione ENTER para continuar...");
        var sVar = leitura.nextLine();
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados); //Comentar esse linha para não savar no db
        //dadosSeries.add(dados); //Comentar quando for salvar no DB.
        repositorio.save(serie); //Comentar esse linha para não savar no db//Comentar esse linha para não savar no db
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        //Linha abaixo comentada, é utilizada quando nao for pesquisar na lista. Ou seja não no banco.
        //DadosSerie dadosSerie = getDadosSerie();
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome:");
        var nomeSerie = leitura.nextLine();
        //asr parei aqui. 05:40 hs

//        Optional<Serie> serie = series.stream()
//                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
//                .findFirst();
        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            //List<Episodio> episodios = temporadas.stream()
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void listarSeriesBuscadas() {
        //List<Serie> series = new ArrayList<>();

//Linhas abaixo devem ser descomentadas para quando for para exibir as serés sem o banco de dados.
//        series = dadosSeries.stream()
//                        .map(d -> new Serie(d))
//                               .collect(Collectors.toList());

        //Linha abaixo para listra o que esta no BD
        //Linha abaixo comentada, é utilizada quando nao for pesquisar na lista. Ou seja não no banco.
        //List<Serie> series = repositorio.findAll();

        //Linhas abaixo para buscar os episódis na serie selecionada.
        series = repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome:");
        var nomeSerie = leitura.nextLine();
        //Optional<Serie> serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        //serieBusca
        if (serieBusca.isPresent()) {
            System.out.println("Dados da série:  " + serieBusca.get());
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    //5 - Buscar séries por ator
    private void buscarSeriePorAtor() {
        System.out.println("Qual o nome do ator para a busca? ");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avalicações a partir de que valor?");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Séries em que " + nomeAtor + " trabalhou: ");
        seriesEncontradas.forEach(s ->
                System.out.println("\nSerie: " + s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
        System.out.println("\n");
    }

    private void buscarTop5Series() {
        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s ->
                System.out.println("\nSerie: " + s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
        System.out.println("\n");
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar séries de que catergoria/Gênero? ");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("\nSéries da categoria " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
        System.out.println("");
    }

    private void filtrarSeriesPorTemporadaEAvaliacao(){
        System.out.println("Filtrar séries até quantas temporadas? ");
        var totalTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Com avaliação a partir de que valor? ");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();
        //List<Serie> filtroSeries = repositorio.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(totalTemporadas, avaliacao);
        List<Serie> filtroSeries = repositorio.seriesPorTemporadaEAvaliacao(totalTemporadas, avaliacao);

        System.out.println("\n*** Séries filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - avaliação: " + s.getAvaliacao()));
        //System.out.println("\n");
        pausaTela();
    }

    //9 - Busca episódio por trecho
    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episódio para a busca? ");
        var trechoEpisodigo = leitura.nextLine();
        List<Episodio> episodiosEncontrados =  repositorio.episodiosPorTrecho(trechoEpisodigo);
        episodiosEncontrados.forEach(e ->
                        System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                                e.getSerie().getTitulo(), e.getTemporada(),
                                e.getNumeroEpisodio(), e.getTitulo()));


        pausaTela();
        //episodiosEncontrados.forEach(System.out::println);
    }

    //10 - Top 5 Episoodios por serie.
    private void tOpEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episódio %s - %s Avaliação %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
            e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }
        pausaTela();
    }
    //

    //11 - Buscar episódios a partir de uma data
    private void buscarEpisodiosDepoisDeUmaData() {
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite de lançamento");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodioAno = repositorio.episodiosPorSereEAno(serie, anoLancamento);
            episodioAno.forEach(System.out::println);

            pausaTela();

        }
    }

}


