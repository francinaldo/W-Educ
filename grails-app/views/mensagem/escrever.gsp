<!DOCTYPE html>
<html>
    
    <head>
        <meta name="layout" content="admin"/>
        <title>Correio- W-Educ</title>
    </head>
    <body>
        <script>
             // Listar usuarios
            var listarUsuarios = function () {
                
                // Inicia requisição assíncrona
                // para listar os usuários
                $.ajax({
                    url: "<g:createLink action="listarUsuarios"/>",
                    type: "post",
                    data: {
                    },
                    success: function (returnData) {
                        bootbox.alert(returnData);
                    },
                    fail: function () {
                        alert("Erro ao tentar listar os usuários cadastrados.");
                    }
                });
            };
            
            // Selecionar destinatário
            var selecionarUsuario = function (id, nome){
                // Inicia requisição assíncrona
                // para acessar o nome do destinatário
                $.ajax({
                    url: "<g:createLink action="selecionarDest"/>",
                    type: "post",
                    data: {
                        id: id
                    },
                    success: function (returnData) {
                        // Substitui o nome do programa
                        $("#destinatario").val(nome);
                    },
                    fail: function () {
                        alert("Erro ao tentar acessar o banco de dados.");
                    }
                });
            };
        </script>    
        
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Correio Eletrônico</h1>
                <div class="form-group">
                    <form action="<g:createLink action="enviar" id="enviar"/>" method="post">
                        
                                <div class="input-group">
                                 <span class="input-group-addon" id="basic-addon2"><b>Para:</b></span> 
                                 <input type="text" class="form-control" id="destinatario" name="destinatario"/>
                                  <span class="input-group-btn">
                                    <button class="btn btn-default" id="selecionar" type="button" onclick="listarUsuarios();">Selecionar Destinatário</button>
                                  </span>
                                </div><!-- /input-group -->
                        <br>
                        <textarea class="form-control" id="mensagem" name="mensagem" required placeholder="Escreva aqui sua mensagem..."></textarea><br/>
                        <input type="submit" class="btn btn-success" value="Enviar mensagem" />
                    </form>
                </div>
            </div>
        </div>
    </body>
</html>
