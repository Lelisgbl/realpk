# Batalha Pokémon

Jogo de batalhas por turnos desenvolvido em Java. O projeto reúne Pokédex, seis famílias evolutivas, golpes com tipos, experiência, evolução automática e salvamento local do progresso.

## Requisitos

- JDK 21 ou compatível.
- Terminal com suporte a entrada pelo teclado.

## Compilar e executar no Windows (PowerShell)

Abra o terminal na pasta `CodigoDescompactado\parcial\realpk` e execute:

```powershell
$classes = "out"
New-Item -ItemType Directory -Force -Path $classes | Out-Null
javac -encoding UTF-8 -d $classes (Get-ChildItem -Recurse -Filter "*.java" | ForEach-Object FullName)
java -cp "$classes;." Main
```

Mantenha `pokemons.json` e `ascii_arts.txt` nessa pasta durante a execução: o jogo lê esses arquivos como recursos locais.

## Como jogar

1. A tela de introdução aparece ao iniciar; pressione Enter para abrir o menu.
2. Selecione **Consultar Pokédex** para ver todas as formas cadastradas.
3. Escolha **Novo jogo** para selecionar uma espécie inicial. A seleção inicial mostra somente as formas-base.
4. Durante a batalha, escolha um golpe digitando seu número. O rival é sorteado entre todas as formas do catálogo.
5. Ao terminar, o jogo salva o Pokémon do jogador. Na próxima execução, escolha **Continuar jogo salvo** para seguir com o progresso.
6. **Resetar progresso salvo** apaga o save após confirmação e permite iniciar outro jogo.

O save é criado como `pokemon-save.properties` no diretório de execução.

## Organização do código

```text
Main.java                  Entrada do programa e menu
model/                     Pokémon, golpes, batalha e efetividade
view/                      Telas da introdução, Pokédex e batalha
infra/                     Leitura do catálogo/artes e save
pokemons.json              Espécies, formas, atributos e golpes
ascii_arts.txt             Artes ASCII identificadas por cabeçalhos
```

## Adicionar Pokémon e evoluções

Cadastre as formas em `pokemons.json`. Cada objeto deve ter ID único, nome, tipo, nível, HP, ataque, defesa, velocidade, `evolutionId` e uma lista `moves` com nome, poder, precisão, tipo e PP.

Use `evolutionId` para apontar à próxima forma. A última forma usa `0`. Exemplo de cadeia:

```text
NovaBase.evolutionId = ID da forma intermediária
NovaIntermediaria.evolutionId = ID da forma final
NovaFinal.evolutionId = 0
```

A seleção de novo jogo detecta formas iniciais pelas relações de evolução: uma forma é inicial quando nenhum outro Pokémon aponta para seu ID. Cadastre ao menos um golpe por Pokémon que possa lutar; o jogo permite até quatro golpes e acrescenta os golpes da forma seguinte ao evoluir.

Tipos sem uma subclasse específica são carregados por `GenericPokemon`. A tabela de efetividade está em `model/TypeEffectiveness.java`; registre as relações do novo tipo com `registerType(...)` e atualize as relações defensivas relevantes. Um tipo sem relação cadastrada é tratado como neutro.

## Adicionar arte ASCII

Em `ascii_arts.txt`, cada imagem começa com um cabeçalho que corresponde ao nome no catálogo:

```text
===NOVAPOKEMON===
   /\\
  ( o )
```

Cadastre um bloco para cada forma. A arte da introdução usa o identificador `INTRO`:

```text
===INTRO===
Sua arte de abertura
```

## Regras principais

- A batalha alterna turnos e usa a velocidade para decidir quem age primeiro.
- O dano considera nível, poder do golpe, ataque, defesa e efetividade de tipos; o divisor final fica em `model/Pokemon.java` (`FINAL_DAMAGE_DIVISOR`).
- Golpes bem-sucedidos concedem EXP. Ao atingir o limite, o Pokémon sobe de nível e evolui usando o ID do catálogo.
- Imunidades são aplicadas pela tabela de tipos, inclusive em Pokémon com dois tipos.

## Testes

Os testes de integração/smoke usados durante o desenvolvimento ficam fora dos pacotes de produção. Para compilar o jogo, use o comando da seção acima; para executar os testes, compile também os arquivos de teste e rode suas classes de teste com o mesmo classpath.


# Batalha Pokémon

Jogo de batalhas por turnos desenvolvido em Java. O projeto reúne Pokédex, seis famílias evolutivas, golpes com tipos, experiência, evolução automática e salvamento local do progresso.

## Requisitos

- JDK 21 ou compatível.
- Terminal com suporte a entrada pelo teclado.

## Compilar e executar no Windows (PowerShell)

Abra o terminal na pasta `CodigoDescompactado\parcial\realpk` e execute:

```powershell
$classes = "out"
New-Item -ItemType Directory -Force -Path $classes | Out-Null
javac -encoding UTF-8 -d $classes (Get-ChildItem -Recurse -Filter "*.java" | ForEach-Object FullName)
java -cp "$classes;." Main
```

Mantenha `pokemons.json` e `ascii_arts.txt` nessa pasta durante a execução: o jogo lê esses arquivos como recursos locais.

## Como jogar

1. A tela de introdução aparece ao iniciar; pressione Enter para abrir o menu.
2. Selecione **Consultar Pokédex** para ver todas as formas cadastradas.
3. Escolha **Novo jogo** para selecionar uma espécie inicial. A seleção inicial mostra somente as formas-base.
4. Durante a batalha, escolha um golpe digitando seu número. O rival é sorteado entre todas as formas do catálogo.
5. Ao terminar, o jogo salva o Pokémon do jogador. Na próxima execução, escolha **Continuar jogo salvo** para seguir com o progresso.
6. **Resetar progresso salvo** apaga o save após confirmação e permite iniciar outro jogo.

O save é criado como `pokemon-save.properties` no diretório de execução.

## Organização do código

```text
Main.java                  Entrada do programa e menu
model/                     Pokémon, golpes, batalha e efetividade
view/                      Telas da introdução, Pokédex e batalha
infra/                     Leitura do catálogo/artes e save
pokemons.json              Espécies, formas, atributos e golpes
ascii_arts.txt             Artes ASCII identificadas por cabeçalhos
```

## Adicionar Pokémon e evoluções

Cadastre as formas em `pokemons.json`. Cada objeto deve ter ID único, nome, tipo, nível, HP, ataque, defesa, velocidade, `evolutionId` e uma lista `moves` com nome, poder, precisão, tipo e PP.

Use `evolutionId` para apontar à próxima forma. A última forma usa `0`. Exemplo de cadeia:

```text
NovaBase.evolutionId = ID da forma intermediária
NovaIntermediaria.evolutionId = ID da forma final
NovaFinal.evolutionId = 0
```

A seleção de novo jogo detecta formas iniciais pelas relações de evolução: uma forma é inicial quando nenhum outro Pokémon aponta para seu ID. Cadastre ao menos um golpe por Pokémon que possa lutar; o jogo permite até quatro golpes e acrescenta os golpes da forma seguinte ao evoluir.

Tipos sem uma subclasse específica são carregados por `GenericPokemon`. A tabela de efetividade está em `model/TypeEffectiveness.java`; registre as relações do novo tipo com `registerType(...)` e atualize as relações defensivas relevantes. Um tipo sem relação cadastrada é tratado como neutro.

## Adicionar arte ASCII

Em `ascii_arts.txt`, cada imagem começa com um cabeçalho que corresponde ao nome no catálogo:

```text
===NOME_POKEMON===
   /\\
  ( o )
```

Cadastre um bloco para cada forma. A arte da introdução usa o identificador `INTRO`:

```text
===INTRO===
Sua arte de abertura
```

## Regras principais

- A batalha alterna turnos e usa a velocidade para decidir quem age primeiro.
- O dano considera nível, poder do golpe, ataque, defesa e efetividade de tipos; o divisor final fica em `model/Pokemon.java` (`FINAL_DAMAGE_DIVISOR`).
- Golpes bem-sucedidos concedem EXP. Ao atingir o limite, o Pokémon sobe de nível e evolui usando o ID do catálogo.
- Imunidades são aplicadas pela tabela de tipos, inclusive em Pokémon com dois tipos.

## Testes

Os testes de integração/smoke usados durante o desenvolvimento ficam fora dos pacotes de produção. Para compilar o jogo, use o comando da seção acima; para executar os testes, compile também os arquivos de teste e rode suas classes de teste com o mesmo classpath.
