On Error Resume Next

Dim fso, scriptDir, javaw, cp, excludedJars, libFolder, files, file, args

Set fso = CreateObject("Scripting.FileSystemObject")
scriptDir = fso.GetParentFolderName(WScript.ScriptFullName)

javaw     = "C:\Program Files\Java\jdk-24\bin\javaw.exe"
libFolder = scriptDir & "\lib"

If Not fso.FileExists(javaw) Then
    MsgBox "No se encontro javaw.exe en:" & vbCrLf & javaw, 16, "Scouteo - Error"
    WScript.Quit 1
End If

If Not fso.FolderExists(libFolder) Then
    MsgBox "No se encontro la carpeta lib en:" & vbCrLf & libFolder, 16, "Scouteo - Error"
    WScript.Quit 1
End If

excludedJars = "|xml-apis-ext-1.3.04.jar|"
cp = scriptDir & "\app.jar"

Set files = fso.GetFolder(libFolder).Files
For Each file In files
    If InStr(excludedJars, "|" & file.Name & "|") = 0 Then
        cp = cp & ";" & file.Path
    End If
Next

If Not fso.FileExists(scriptDir & "\app.jar") Then
    MsgBox "No se encontro app.jar en:" & vbCrLf & scriptDir, 16, "Scouteo - Error"
    WScript.Quit 1
End If

args = " --module-path """ & libFolder & """" & _
       " --add-modules javafx.base,javafx.controls,javafx.graphics,javafx.fxml,javafx.web,javafx.swing,javafx.media" & _
       " --add-exports javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED" & _
       " --add-exports javafx.web/com.sun.javafx.webkit=ALL-UNNAMED" & _
       " --add-exports javafx.web/com.sun.webkit=ALL-UNNAMED" & _
       " --add-exports javafx.web/com.sun.javafx.webkit.prism=ALL-UNNAMED" & _
       " --add-opens javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED" & _
       " --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED" & _
       " --add-opens javafx.web/javafx.scene.web=ALL-UNNAMED" & _
       " --enable-native-access=javafx.graphics,javafx.web" & _
       " -cp """ & cp & """" & _
       " com.javafx.scouteo.Main"

Dim shell, exitCode
Set shell = CreateObject("WScript.Shell")
exitCode = shell.Run("""" & javaw & """" & args, 1, True)

If exitCode <> 0 Then
    MsgBox "La aplicacion cerro con error: " & exitCode, 16, "Scouteo - Error"
End If
