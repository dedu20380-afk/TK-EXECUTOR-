package com.tk.executor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val Bg = Color(0xFF0B0910)
private val Panel = Color(0xFF171320)
private val Panel2 = Color(0xFF211A2D)
private val Purple = Color(0xFF9B5CFF)
private val Muted = Color(0xFFAAA1B8)
private val White = Color(0xFFF7F3FC)

data class Script(val id: Long, val name: String, val code: String)

class ScriptStore(private val activity: MainActivity) {
    private val prefs = activity.getSharedPreferences("tk_scripts", 0)
    fun all(): List<Script> {
        val arr = JSONArray(prefs.getString("items", "[]"))
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Script(o.optLong("id"), o.optString("name"), o.optString("code"))
        }
    }
    fun save(script: Script) {
        val list = all().filterNot { it.id == script.id } + script
        val arr = JSONArray()
        list.forEach { s -> arr.put(JSONObject().put("id", s.id).put("name", s.name).put("code", s.code)) }
        prefs.edit().putString("items", arr.toString()).apply()
    }
    fun delete(id: Long) {
        val arr = JSONArray()
        all().filterNot { it.id == id }.forEach { s ->
            arr.put(JSONObject().put("id", s.id).put("name", s.name).put("code", s.code))
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = ScriptStore(this)
        setContent { TKApp(store) }
    }
}

@Composable
fun TKApp(store: ScriptStore) {
    var page by remember { mutableStateOf("Editor") }
    var scripts by remember { mutableStateOf(store.all()) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var scriptName by remember { mutableStateOf("Meu script") }
    var code by remember { mutableStateOf("-- TK Executor | ambiente de testes\n\nprint(\"Olá, mundo!\")") }
    var console by remember { mutableStateOf("Console pronto. O runtime Luau será integrado em uma próxima etapa.") }
    var dark by remember { mutableStateOf(true) }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Purple, onBackground = White,
        onSurface = White, secondary = Muted
    )) {
        Column(Modifier.fillMaxSize().background(Bg)) {
            Header()
            when (page) {
                "Editor" -> EditorScreen(
                    name = scriptName, onName = { scriptName = it },
                    code = code, onCode = { code = it },
                    console = console,
                    onSave = {
                        val id = selectedId ?: System.currentTimeMillis()
                        selectedId = id
                        store.save(Script(id, scriptName.ifBlank { "Sem título" }, code))
                        scripts = store.all()
                        console = "Script salvo localmente: ${scriptName.ifBlank { "Sem título" }}"
                    },
                    onRun = { console = "Execução indisponível nesta versão. O editor e o armazenamento estão ativos; o runtime Luau isolado será adicionado após testes de integração." },
                    onClear = { code = ""; console = "Editor limpo." }
                )
                "Biblioteca" -> LibraryScreen(
                    scripts = scripts,
                    onOpen = { s -> selectedId = s.id; scriptName = s.name; code = s.code; page = "Editor" },
                    onDelete = { id -> store.delete(id); scripts = store.all() }
                )
                else -> SettingsScreen(dark = dark, onDark = { dark = it })
            }
            Spacer(Modifier.weight(1f))
            NavigationBar(containerColor = Panel, contentColor = White) {
                listOf("Editor", "Biblioteca", "Ajustes").forEach { item ->
                    NavigationBarItem(
                        selected = page == item,
                        onClick = { page = item; if (item == "Biblioteca") scripts = store.all() },
                        icon = {
                            Icon(
                                when (item) {
                                    "Editor" -> Icons.Default.Code
                                    "Biblioteca" -> Icons.Default.Folder
                                    else -> Icons.Default.Settings
                                }, contentDescription = item
                            )
                        },
                        label = { Text(item) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Purple, selectedTextColor = Purple,
                            indicatorColor = Panel2, unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun Header() {
    Row(
        Modifier.fillMaxWidth().background(Panel).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).background(Purple, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text("TK", color = White, fontWeight = FontWeight.Black, fontSize = 17.sp)
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text("TK EXECUTOR", color = White, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text("SCRIPT WORKSPACE  •  ANDROID", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        }
        Spacer(Modifier.weight(1f))
        Surface(color = Color(0xFF243528), shape = RoundedCornerShape(50)) {
            Text("v0.1.0", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Color(0xFF8BE0A0), fontSize = 10.sp)
        }
    }
}

@Composable
fun EditorScreen(
    name: String, onName: (String) -> Unit, code: String, onCode: (String) -> Unit,
    console: String, onSave: () -> Unit, onRun: () -> Unit, onClear: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("EDITOR", color = White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Text("Escreva e organize seus scripts", color = Muted, fontSize = 12.sp)
            }
            Surface(color = Panel2, shape = RoundedCornerShape(8.dp)) {
                Text("LUA / LUAU", Modifier.padding(9.dp), color = Purple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        OutlinedTextField(
            value = name, onValueChange = onName, singleLine = true,
            label = { Text("Nome do script") }, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple, unfocusedBorderColor = Panel2,
                focusedTextColor = White, unfocusedTextColor = White,
                focusedLabelColor = Purple, unfocusedLabelColor = Muted
            )
        )
        Column(Modifier.weight(1f).fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("●", color = Color(0xFFFF6B75), fontSize = 10.sp)
                Text("  ●", color = Color(0xFFFFC45C), fontSize = 10.sp)
                Text("  ●", color = Color(0xFF6DDB91), fontSize = 10.sp)
                Spacer(Modifier.weight(1f))
                Text("main.lua", color = Muted, fontSize = 10.sp)
            }
            Spacer(Modifier.height(12.dp))
            BasicTextField(
                value = code, onValueChange = onCode,
                modifier = Modifier.fillMaxSize(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFFE3D7F4), fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace, lineHeight = 20.sp
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Purple),
                decorationBox = { inner ->
                    Box {
                        if (code.isEmpty()) Text("Digite seu código Lua/Luau...", color = Muted, fontFamily = FontFamily.Monospace)
                        inner()
                    }
                }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRun, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text("Executar")
            }
            OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Save, null); Spacer(Modifier.width(5.dp)); Text("Salvar")
            }
            OutlinedButton(onClick = onClear) { Icon(Icons.Default.Delete, null) }
        }
        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(12.dp)) {
            Text("CONSOLE", color = Purple, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Spacer(Modifier.height(5.dp))
            Text(console, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
fun LibraryScreen(scripts: List<Script>, onOpen: (Script) -> Unit, onDelete: (Long) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("BIBLIOTECA", color = White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        Text("${scripts.size} script(s) salvos neste dispositivo", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        if (scripts.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, null, tint = Muted, modifier = Modifier.size(42.dp))
                    Text("Sua biblioteca está vazia", color = Muted)
                    Text("Salve um script pelo editor para vê-lo aqui.", color = Muted, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(scripts, key = { it.id }) { script ->
                    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).clickable { onOpen(script) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, null, tint = Purple)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(script.name, color = White, fontWeight = FontWeight.SemiBold)
                            Text("${script.code.lines().size} linhas", color = Muted, fontSize = 10.sp)
                        }
                        IconButton(onClick = { onDelete(script.id) }) { Icon(Icons.Default.Delete, null, tint = Muted) }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(dark: Boolean, onDark: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("CONFIGURAÇÕES", color = White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tema escuro", color = White, fontWeight = FontWeight.SemiBold)
                Text("Interface escura estilo gaming", color = Muted, fontSize = 11.sp)
            }
            Switch(checked = dark, onCheckedChange = onDark)
        }
        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(14.dp)) {
            Text("Sobre o TK Executor", color = White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text("Versão 0.1.0 • Projeto em desenvolvimento", color = Muted, fontSize = 12.sp)
            Text("Esta versão oferece editor e biblioteca local. A execução Luau será adicionada em uma etapa própria, com runtime isolado.", color = Muted, fontSize = 11.sp)
        }
    }
}
