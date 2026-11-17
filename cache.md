## O que é cache e qual problema ele resolve?

Cache é uma camada de armazenamento temporário e rápido que mantém cópias de dados usados com frequência. Quando uma cópia está disponível e válida, o sistema pode usá-la em vez de buscar ou calcular o dado novamente na fonte original.

 **O que melhora:** tempo de resposta, uso da rede e carga sobre servidores e bancos de dados. O cache costuma usar memória (RAM), mas também pode usar armazenamento local, como SSD ou disco.

### Dois exemplos do dia a dia

1. **Navegador:** guarda imagens, CSS e JavaScript no dispositivo do usuário, em memória e/ou no disco. Ao revisitar uma página, pode reutilizar esses arquivos sem baixá-los todos de novo.
2. **CDN:** mantém cópias de imagens, vídeos e outros arquivos em servidores próximos dos usuários. Isso diminui a distância da transferência e reduz as solicitações que chegam ao servidor de origem.

 **Outras camadas:** a aplicação pode guardar respostas de uma API na RAM ou em Redis; o banco de dados pode manter páginas de dados acessadas com frequência na memória.

## Cache hit, cache miss e hit ratio

- **Cache hit (acerto):** o dado procurado está no cache e pode ser usado, evitando em geral uma consulta à fonte original.
- **Cache miss (falha):** o dado não está no cache, foi removido ou expirou. O sistema busca o dado na fonte original e pode guardar uma cópia para as próximas solicitações.
- **Hit ratio (taxa de acerto):** proporção das buscas no cache que foram acertos. Uma taxa maior costuma indicar que o cache está atendendo bem às solicitações, mas não garante, sozinha, que os dados estejam atualizados.

> **Fórmula:** `Hit ratio (%) = hits ÷ (hits + misses) × 100`  
> **Exemplo:** 90 hits e 10 misses resultam em um hit ratio de **90%**.

## TTL e dados stale

**TTL (Time To Live)** é o tempo de validade atribuído a um dado no cache, normalmente medido em segundos. Quando o TTL termina, o cache pode remover o dado ou exigir que ele seja buscado/revalidado novamente.

Um dado fica **stale** (velho ou desatualizado) quando a fonte original muda, mas o cache ainda guarda uma cópia anterior. Sem invalidação ou atualização imediata, essa cópia pode continuar sendo usada até o TTL vencer. Algumas estratégias também permitem servir uma cópia stale enquanto atualizam o cache em segundo plano.

- **TTL longo:** pode aumentar os acertos e reduzir a carga na fonte, mas aumenta o tempo em que uma alteração pode não aparecer. Faz sentido para conteúdo que muda pouco, como imagens e arquivos estáticos versionados.
- **TTL curto:** limita o tempo de desatualização, mas causa mais misses e consultas à fonte. Pode ser necessário para informações que mudam com frequência, como estoque. Para dados críticos, o TTL sozinho pode não ser suficiente: pode ser preciso invalidar o cache quando o dado muda.

## Políticas de remoção (eviction): FIFO, LRU e LFU

Essas políticas decidem **qual item remover quando o cache precisa liberar espaço**. São diferentes do TTL, que determina por quanto tempo um item é considerado válido.

| Política | Como funciona | Quando faz mais sentido |
| --- | --- | --- |
| 📥 **FIFO** (First In, First Out) | Remove primeiro o item que está há mais tempo no cache, sem considerar quantas vezes foi usado recentemente. | Quando se quer uma regra simples e a idade do item é um critério razoável para removê-lo. |
| 🕒 **LRU** (Least Recently Used) | Remove o item que ficou há mais tempo sem ser acessado. | Quando dados acessados recentemente têm maior chance de serem usados de novo, como em muitos padrões de navegação. |
| 🔢 **LFU** (Least Frequently Used) | Remove o item com menos acessos em um período ou conforme um contador de frequência. | Quando há um conjunto estável de itens muito populares que deve permanecer no cache. Se a popularidade muda, acessos antigos podem influenciar demais. |

Não existe uma política ideal para todos os casos: a escolha depende do padrão de acesso, do custo de buscar os dados e da implementação do cache.

## 🔄 Invalidação de cache: por que é difícil?

**Invalidar o cache** é remover ou atualizar uma cópia armazenada quando o dado original muda, para evitar que o sistema continue usando uma versão antiga. Isso pode ser feito apagando a chave correspondente, substituindo seu valor ou usando versões nas chaves.

É difícil porque o mesmo dado pode estar copiado em vários lugares — em diferentes servidores, navegadores ou CDNs — e todas essas cópias precisam ser atualizadas de forma coordenada. Também podem ocorrer concorrências:

1. Uma solicitação lê um valor antigo da fonte.
2. Outra operação atualiza a fonte e invalida a chave do cache.
3. A primeira solicitação grava o valor antigo no cache depois da invalidação.

⚠️ **Resultado:** a cópia antiga reaparece. Por isso, é preciso equilibrar consistência, desempenho e complexidade. O TTL reduz por quanto tempo uma cópia pode permanecer, mas não garante que todas as cópias sejam atualizadas imediatamente.

## 🏠 Cache local vs. cache distribuído

| Tipo | Características | Limitações |
| --- | --- | --- |
| 🏠 **Local** | Fica na memória do processo da aplicação. É rápido e simples de acessar, sem uma chamada de rede adicional. | Cada instância da aplicação tem sua própria cópia; as cópias podem divergir. Os dados normalmente se perdem quando o processo reinicia, e o espaço é limitado à memória dessa instância. |
| ☁️ **Distribuído** (ex.: Redis ou Memcached) | Fica em um serviço separado, acessível por várias instâncias da aplicação, que podem compartilhar o mesmo cache. | Exige comunicação pela rede e operação de um serviço adicional; é preciso considerar latência, disponibilidade e configuração. |

### Quando o cache local deixa de ser suficiente?

- Há várias instâncias da aplicação que precisam compartilhar os mesmos dados.
- As cópias locais começam a divergir ou a duplicação ocupa memória demais.
- O conjunto de dados excede a capacidade local.

Um cache distribuído pode ajudar nesses casos. Ainda assim, os dois tipos podem ser usados juntos: o local para acessos muito frequentes e o distribuído como camada compartilhada.
