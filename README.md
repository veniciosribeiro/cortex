# Currency conversion for Cortex Case

TESTE DE CI DO GITHUB

Esta aplicação pode ser acessada no Heroku através do link de exemplo abaixo:

[https://cortex-backend-conversion-api.herokuapp.com/converte/USD/BRL/1/20-10-2020](https://cortex-backend-conversion-api.herokuapp.com/converte/USD/BRL/1/20-10-2020)

## Execução da aplicação

## Docker

É possível inicializar a aplicação usando `docker-composer` ou `docker run`.

#### docker-compose

Baixe o arquivo [docker-compose.yml](docker-compose.yml) e configure o arquivo `.env` com as configurações da aplicação, em seguida execute os comandos listados abaixo.

Dentre outras informações, o aquivo `.env` contém informações de conexão com o banco de dados, que é necessário para a aplicação iniciar. O arquivo precisa estar na mesma pasta do `docker-compose.yml`.

|Variável|Descrição|
|:---|:---|
| DATABASE_POSTGRES_URL | Url do Banco de dados|
| DATABASE_POSTGRES_USER | Usuário de conexão com o banco de dados |
| DATABASE_POSTGRES_PASS | Senha de conexão com o banco de dados |
| RABBIT_HOST | Endereço do RabbitMQ |
| RABBIT_VHOST | Endereço virtual host do RabbitMQ | 
| RABBIT_USER | Usuário de conexão com o RabbitMQ | 
| RABBIT_PASS | Senha do servidor RabbitMQ |
| RABBIT_EXC | Exchange utilizada pelo RabbitMQ |
| RABBIT_RTK | Routing key utilizada pelo RabbitMQ |
| LOG_LEVEL_ROOT | Root log level do Spring | 
| LOG_LEVEL_WEB | Log level de requisições WEB | 
| LOG_LEVEL_SPRING | Log level do Spring | 
| API_BC_CONEXAO_TIMEOUT | Timeout de conexão com o servidor do banco central |
| API_BC_CACHE_TIMEOUT | Tempo de vida do cache de conversões em minutos |
| TZ | Timezone da aplicação. Ex.:`America/Sao_Paulo` |
| SPRING_PROFILES_ACTIVE | Profile que será utilizado pela aplicação | 
| PORT | Server Port que será utilizada |

Para baixar a imagem mais recente, execute:
```bash
$ docker-compose pull
```

Para iniciar a aplicação, execute o comando abaixo:

`-d` roda a aplicação em background.<br>

```bash
$ docker-compose up -d
```

Para parar a aplicação e remover o container, networks, volumes, e imagens criadas pelo comando `up`, execute:

```bash
$ docker-compose down
```

#### docker run

Para baixar a imagem mais recente, execute:
```bash
$ docker pull mvrdutra/cortex-currency-conversion:latest
```

Para iniciar a aplicação, execute o comando abaixo:

`-d` roda a aplicação em background.<br>
`--rm` remove automaticamente o container.<br>
`--name` atribui o nome ao container.<br>
`--env-file` caminho do arquivo `.env` que contém as configurações necessárias.

```bash
$ docker run -p 8080:8080 --env-file .env -d --rm --name cortex-currency-conversion mvrdutra/cortex-currency-conversion:latest
```

Para parar a aplicação, execute o comando abaixo:

```bash
$ docker stop cortex-currency-conversion
```

##

#### Moedas disponíveis para consulta
* Coroa dinamarquesa (DKK) Tipo A
* Coroa norueguesa (NOK) Tipo A
* Coroa sueca (SEK) Tipo A
* Dólar americano (USD) Tipo A
* Dólar australiano (AUD) Tipo B
* Dólar canadense (CAD) Tipo A
* EURO (EUR) Tipo B
* Franco suíço (CHF) Tipo A
* Iene (JPY) Tipo A
* Libra esterlina (GBP) Tipo B

#### Swagger
O Swagger pode ser acessado no endereço abaixo ou acessando o caminho `/swagger-ui.html` em outro ambiente configurado.

- https://cortex-backend-conversion-api.herokuapp.com/swagger-ui.html

## 
#### Diagrama Producer:
![Diagrama Producer](https://raw.githubusercontent.com/veniciosribeiro/cortex/master/documentacao/Rabbit-Producer.png "Diagrama de Aplicação e Fluxo do Producer")

##### Diagrama Consumer
![Diagrama Consumer](https://raw.githubusercontent.com/veniciosribeiro/cortex/master/documentacao/Rabbit-Consumer.png "Diagrama de Aplicação e Fluxo do Consumer")

#### Assertividade
##### Priorização
Devido a alguns contratempos não foi possível implementar a priorização de consumo da fila, porém haviam duas opções: 
- Criar um `schedule` consumindo as entradas do banco de dados ordenando por prioridade e data de solicitação. Em seguida enviar para a fila e aguardar a conversão.
- Utilizar a `Priority Queue` do RabbitMQ com `x-max-priority` para gerir o enfileiramento e envio para os consumers.

##### Separação em mais serviços
Tive alguns problemas pessoais de saúde durante a execução do desafio que me fez ter menos tempo para separar melhor o escopo da aplicação.

O ideal seria:
- Aplicação 1: Recebe as requisições e salva no banco.
- Aplicação 2: Carrega os itens do banco por priorização e data e envia para a fila.
- Aplicação 3: Consome a fila e realiza a conversão.
- Aplicações extras: Coletor de logs, desempenho e talvez um Gateway.

#### Para refências externas, considere as seguintes explicações do Banco Central:

* [Guia de consulta da API do Banco Central](https://dadosabertos.bcb.gov.br/dataset/taxas-de-cambio-todos-os-boletins-diarios)
* [Cotações diárias e Taxas de Câmbio](https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/aplicacao#!/recursos/CotacaoMoedaDia)

