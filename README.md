# TK Executor — Android v0.1.0

Primeira base funcional do projeto, feita com Kotlin e Jetpack Compose.

## O que já funciona
- Navegação entre Editor, Biblioteca e Ajustes.
- Edição de texto Lua/Luau.
- Salvar scripts localmente usando SharedPreferences.
- Abrir e excluir scripts salvos.
- Console de estado.
- Interface escura roxa.

## O que ainda não está implementado
- Runtime Luau real.
- Destaque de sintaxe e numeração de linhas.
- Importar/exportar arquivos.
- Testes automatizados e assinatura de release.

O botão Executar informa claramente que o runtime ainda não foi integrado. Não injeta scripts no Roblox nem contorna proteções.

## Como abrir
1. Instale o Android Studio.
2. Abra a pasta `TKExecutor`.
3. Aguarde a sincronização do Gradle.
4. Use JDK 17.
5. Execute em um emulador ou celular Android.

Compile pelo terminal:
`gradle assembleDebug`

O APK de debug, quando compilado, fica em:
`app/build/outputs/apk/debug/app-debug.apk`
