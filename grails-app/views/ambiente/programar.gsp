<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="admin"/>
        <title>Ambiente Textual - W-Educ</title>
        <style type="text/css" media="screen">
            #editor { 
                height: 600px;
                position: absolute;
                top: 8em;
                right: 0;
                bottom: 0;
                left: 0;
            }
        </style>
    </head>
    <body>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Programar em ${linguagem?.name}</h1>
                <div id="editor">// Olá! Comece a programar aqui.</div>
            </div>
        </div>
            
        <asset:javascript src="js/ace/ace.js"/>
        <script>
            var editor = ace.edit("editor");
            editor.setTheme("ace/theme/github");
            editor.getSession().setMode("ace/mode/c_cpp");
            var editor_height = $("#editor_container").height();
            $("#editor").height(editor_height);
        </script>
    </body>
</html>
