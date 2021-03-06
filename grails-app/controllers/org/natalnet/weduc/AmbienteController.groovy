package org.natalnet.weduc

import com.roboeduc.compiladorreduc.*
import java.io.File
import groovy.io.FileType

import grails.plugin.springsecurity.annotation.Secured

class AmbienteController {

	def springSecurityService
        
        def saveCompile = true;
      
    
	def index() {}
        
        //programar
	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
	def programar() {
                
                // Funções
            def funcoes = Funcao.findAllWhere(linguagem: Linguagem.get(params.id))
            
            [linguagem: Linguagem.get(params.id), funcoes: funcoes]    
        }

        //salvarPrograma
	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
	def salvarPrograma() {   

                if (params.nome == "")
                    return
                    
        
                System.out.println(CommandShellToString.execute("pwd"));
		// Define usuário atual
		def usuario = springSecurityService.getCurrentUser()

		// Define a linguagem
		def linguagem = Linguagem.get(params.linguagem)

		// Define se é R-Educ ou não
		def reduc = params.reduc == "1" ? true : false

		// Procura um programa de mesmo nome;
		// caso não exista, o cria
		def programa = Programa.findOrCreateWhere(
			usuario: usuario, 
			linguagem: linguagem,
			reduc: reduc,
			nome: params.nome
		)	

		// Registra data de criação, 
		// caso programa seja novo
		programa.criadoEm = programa.criadoEm ? programa.criadoEm : (new Date())

		// Registra data de modificação
		programa.modificadoEm = new Date()

		// Inicialização das estatísticas
		programa.compilacoes = programa.compilacoes ? programa.compilacoes : 0
		programa.compilacoesBemSucedidas = programa.compilacoesBemSucedidas ? programa.compilacoesBemSucedidas : 0
		programa.compilacoesMalSucedidas = programa.compilacoesMalSucedidas ? programa.compilacoesMalSucedidas : 0

		// Define se é R-Educ 
		programa.reduc = reduc

		// Define extensão
		programa.extensao = reduc ? ".rob" : linguagem.extension

		// Define o nome
		programa.nome = programa.nome ? programa.nome : params.nome

		// Define o código
		programa.codigo = params.codigo 

		// Salva o programa
		programa.save(flush: true)

                if (saveCompile)
                    
                    render "OK"
	}
        
        //solicitarCorrecao
	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
	def solicitarCorrecao() {
            
            //Define usuário atual
           def usuario = springSecurityService.getCurrentUser()
           
           if (Aluno.get(usuario.id).professor){
                def professor = Aluno.get(usuario.id).professor.username
                def professorId = Aluno.get(usuario.id).professor.id
               // Define a linguagem
               def linguagem = Linguagem.get(params.linguagem)

               // Define se é R-Educ ou não
               def reduc = params.reduc == "1" ? true : false

               def codigo = params.codigo
               def conteudo
            
               if (reduc){ 
                    conteudo = "Caro professor, solicito a correção do código escrito em R-Educ para a linguagem" + linguagem.name + " ."
                    conteudo = conteudo + "\n  Código: \n"
                    conteudo = conteudo + codigo
               }
               else{
                    conteudo = "Caro professor, solicito a correção do código escrito diretamente na linguagem " + linguagem.name + " ."
                    conteudo = conteudo + "\n  Código: \n"
                    conteudo = conteudo + codigo           
               }
                
               def mensagem = new Mensagem()
               mensagem.destinatario = Usuario.findByUsername(professor)
               mensagem.autor = usuario
               mensagem.data = new Date()
               mensagem.mensagem = conteudo
               mensagem.save(flush: true, failOnError: true)
                
                render "Correção solicitada com sucesso."

            }
            
            else
                render "Não foi possível solicitar a correção pois você ainda não possui um professor."
        }

        //compilarPrograma
	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
	def compilarPrograma() {
                
                if (params.nome == ""){
                    render "É preciso nomear e salvar o programa para compilar."
                    return
            
                }
                //salva o programa antes de compilar
                saveCompile = false;
                salvarPrograma();
                saveCompile = true;
                
		// Define usuário atual
		def usuario = springSecurityService.getCurrentUser()

		// Define a linguagem
		def linguagem = Linguagem.get(params.linguagem)

		// Define se é R-Educ ou não
		def reduc = params.reduc == "1" ? true : false

		// Procura um programa de mesmo nome;
		// caso não exista, o cria
		def programa = Programa.findWhere(
			usuario: usuario, 
			linguagem: linguagem,
			reduc: reduc,
			nome: params.nome
		)
	
	//Apaga todos os arquivos da pasta do usuário
	File fDelete = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username)
	fDelete.deleteDir()
      	    

		// Salva programa em arquivo temporário
        String fName = programa.nome;
   
	File c = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username)
	if (!c.exists()) {
            c.mkdirs()
            }  

        def pontuacao
        if(reduc)
            pontuacao = ""
        else
            pontuacao = "."
        File f = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName + pontuacao + programa.extensao)
        f << programa.codigo //.replaceAll("\n", "\r\n");


	if(reduc) { 	// Verifica se é R-Educ

	// Compila o arquivo utilizando o compilador REduc
            
            // Define o local do programa, para utilização futura
            programa.local = fName + programa.extensao + "." + linguagem.extension

            def language;
            language = new Language();
            language.setId(linguagem.id);
            language.setName(linguagem.name);
            language.setDescription(linguagem.description);
            language.setRobot(linguagem.robot);
            language.setExtension(linguagem.extension);

            language.setCompileCode(linguagem.compileCode);
            language.setCompilerFile(linguagem.compilerFile);
            language.setSendCode(linguagem.sendCode);
            language.setSentExtension(linguagem.sentExtension);

            language.setHeader(linguagem.header);
            language.setFootnote(linguagem.footnote);

            language.setMainFunction(linguagem.mainFunction);
            language.setOtherFunctions(linguagem.otherFunctions);
            language.setCallFunction(linguagem.callFunction);

            // Tipos
            def tipos = Tipos.findWhere(id: linguagem.types.id)
            def types;
            types = new Types();
            types.setId(tipos.id);
            types.setName(tipos.name);
            types.setDeclareFalse(tipos.declareFalse);
            types.setDeclareTrue(tipos.declareTrue);
            types.setDeclareFloat(tipos.declareFloat);
            types.setDeclareString(tipos.declareString);
            types.setDeclareBoolean(tipos.declareBoolean);
            language.setTypes(types);

            // Funções
            def funcoes = Funcao.findAllWhere(linguagem: linguagem)

            def functions = new ArrayList();

            funcoes.each() {
                obj ->

                def function = new LFunction();

                function.setId(obj.id);
                function.setName(obj.name);
                function.setType(obj.type);
                function.setReturnType(obj.returnType);
                function.setQntParameters(obj.qntParameters);
                function.setCode(obj.code);
                function.setDescription(obj.description);
                function.setTypeAliases(obj.typeAliases);
                function.setImageURL(obj.imageURL);
                functions.add(function);
            }

            // Defines
            def defines = Definicao.findAllWhere(linguagem: linguagem)

            def defines2 = new ArrayList();

            defines.each() {
                obj ->

                def defines3 = new Defines();
                
                defines3.setId(obj.id);
                defines3.setName(obj.name);
                defines3.setType(obj.type);
                defines3.setText(obj.text);
                defines3.setAlreadyExists(obj.alreadyExists);
                defines2.add(defines3);
            }

            // Operadores
            def operadores = Operadores.findWhere(id: linguagem.operators.id)
            def operators;
            operators = new Operators();
            operators.setId(operadores.id);
            operators.setEqualTo(operadores.equalTo);
            operators.setNotEqualTo(operadores.notEqualTo);
            operators.setGreaterThan(operadores.greaterThan);
            operators.setLessThan(operadores.lessThan);
            operators.setLessThanOrEqualTo(operadores.lessThanOrEqualTo);
            operators.setGreaterThanOrEqualTo(operadores.greaterThanOrEqualTo);
            operators.setLogicalAnd(operadores.logicalAnd);
            operators.setLogicalOr(operadores.logicalOr);
            operators.setLogicalNot(operadores.logicalNot);
            operators.setName(operadores.name);

            // Controle de fluxo
            def controleDeFluxo = ControleDeFluxo.findWhere(id: linguagem.controlFlow.id)
            ControlFlow controlFlow;
            controlFlow = new ControlFlow();
            controlFlow.setId(controleDeFluxo.id);
            controlFlow.setLanguageName(controleDeFluxo.languageName);
            controlFlow.setBreakCode(controleDeFluxo.breakCode);
            controlFlow.setDoCode(controleDeFluxo.doCode);
            controlFlow.setForCode(controleDeFluxo.forCode);
            controlFlow.setIfCode(controleDeFluxo.ifCode);
            controlFlow.setRepeatCode(controleDeFluxo.repeatCode);
            controlFlow.setSwitchCode(controleDeFluxo.switchCode);
            controlFlow.setWhileCode(controleDeFluxo.whileCode);

            def lexico = new analisadorLexico();
            lexico.readFile(f.path);
            def sintatico = new analisadorSintatico(lexico, "/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName, programa.extensao, linguagem.name, language.extension);            
            sintatico.getMapeamento().defineValues(language, types, functions, operators, controlFlow, defines2);
            lexico.defineUsedStructures(sintatico);
            
            try{


                sintatico.startCompile();
                sintatico.closeFile();
                // println(sintatico.isError());
                // render sintatico.isError();
            }
            catch (Exception e) {
            	//println e
                render "Erro ao compilar o programa. Consulte o administrador do sistema! \n"
                return;    
            }


            //Apaga todos os arquivos da pasta da linguagem para realizar nova compilação
	    File fDell = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName)
	    fDell.deleteDir()
      	    

	   //Copia os arquivos de include e extrai na pasta
	   try {		 
		def origem = "/data/sites/weduc/weduc/arquivos-de-include/" + linguagem.id +  "/arquivo"	
		def destino = "/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName        
                CommandShellToString.execute("unzip "+origem+" -d "+ destino);              
	   }
	   catch (Exception e) {
            	println e
            }

            // Deixa o arquivo com a extensão e identificação desejadas
            File fSource = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName + programa.extensao + "." + linguagem.extension)
            File fTarget = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName + "/" + fName + "." + linguagem.extension)
            org.apache.commons.io.FileUtils.copyFile(fSource, fTarget);

            // Define o arquivo onde ficarão os comandos do Make
            File fShell = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username +"/"+ fName +"/weduc.sh")
	    
            //Colocar comando no servidor!
            //def comando = "(Xvfb :1 -nolisten tcp -screen :1 1280x800x24 &);DISPLAY=:1 " + linguagem.compileCode + ";   killall Xvfb"
	    def comando = "DISPLAY=:1 " + linguagem.compileCode 
            println comando;
            comando = comando.replace("diretorio", "/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username +"/"+ fName)
            comando = comando.replace("localdocompilador", "/data/sites/weduc/weduc/arquivos-de-compilacao/" + linguagem.id )
            comando = comando.replace("nomedoprograma",  fName )
            org.apache.commons.io.FileUtils.writeStringToFile(fShell, comando, null)
               
            println comando;  
            // Prepara o comando Make
            comando = "/bin/bash /data/sites/weduc/tmp/weduc/compilador/" 
            comando += usuario?.username + "/" + fName + "/weduc.sh"


            println "------>"
            println comando;
            println "------>"
 

            try {

		System.out.println("Iniciei a compilacao na linguagem " + linguagem.id);
                System.out.println(CommandShellToString.execute(comando));
                
		 System.out.println("Finalizei a compilacao na linguagem " + linguagem.id);

            }
            catch (IOException ex) {
                System.out.println("Erro ao compilar o programa.");
            }
            catch (Exception e) {
            	println e
            }

            
            if(!sintatico.isError()) {
                programa.compilacoesBemSucedidas = programa.compilacoesBemSucedidas + 1;
                programa.save(flush: true)
                render "Compilação bem sucedida!"
            } else {
                //Adicionar aqui a equação das categorias.
                categorizarErros(sintatico.getErrorType(), lexico.getUsedStructures(), "1", "variavel");
                categorizarErros(sintatico.getErrorType(), lexico.getUsedStructures(), "2", "funcao");
                categorizarErros(sintatico.getErrorType(), lexico.getUsedStructures(), "3", "tarefa");
                categorizarErros(sintatico.getErrorType(), lexico.getUsedStructures(), "4", "estrutura");
                categorizarErros(sintatico.getErrorType(), lexico.getUsedStructures(), "5", "condicao");
                categorizarErros(sintatico.getErrorType(), lexico.getUsedStructures(), "6", "repeticao");
                categorizarErros(sintatico.getErrorType(), lexico.getUsedStructures(), "7", "nome");
                categorizarErros(sintatico.getErrorType(), lexico.getUsedStructures(), "8", "sintaxe");
                programa.compilacoesMalSucedidas = programa.compilacoesMalSucedidas + 1;
                programa.save(flush: true)
                render "Linha: " + sintatico.getErrorInt() + " -> Erro " + sintatico.getErrorStr();
            }
	} //Termina Compilação em R-Educ

	// Se não for a linguagem alvo:
	else {
            
            //Apaga todos os arquivos da pasta da linguagem para realizar nova compilação
	    File fDell = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + linguagem.id)
	    fDell.deleteDir()
            
            //Copia os arquivos de include e extrai na pasta
            try {		 
                 def origem = "/data/sites/weduc/weduc/arquivos-de-include/" + linguagem.id +  "/arquivo"	
                 def destino = "/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName        
                 CommandShellToString.execute("unzip "+origem+" -d "+destino);              
            }
            catch (Exception e) {
                 println e
             }
             
            File fTarget = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName + "/" + fName + "." + linguagem.extension);
            org.apache.commons.io.FileUtils.writeStringToFile(fTarget, programa.codigo, null)
            
            // Deixa o arquivo com a extensão e identificação desejadas
            //File fSource = new File("/tmp/weduc/compilador/" + usuario?.username + "/" + fName + programa.extensao + "." + linguagem.extension)
            //File fTarget = new File("/tmp/weduc/compilador/" + usuario?.username + "/" + linguagem.id + "/" + fName + "." + linguagem.extension)
            //org.apache.commons.io.FileUtils.copyFile(fSource, fTarget);

            // Define o arquivo onde ficarão os comandos do Make
            File fShell = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username +"/"+ fName +"/weduc.sh")
	    def comando = "DISPLAY=:1 " + linguagem.compileCode 

	    println comando;
            comando = comando.replace("diretorio", "/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username +"/"+ fName)
            comando = comando.replace("localdocompilador", "/data/sites/weduc/weduc/arquivos-de-compilacao/" + linguagem.id )
            comando = comando.replace("nomedoprograma",  fName )
            org.apache.commons.io.FileUtils.writeStringToFile(fShell, comando, null)
               
            println comando;  
            // Prepara o comando Make
            comando = "/bin/bash /data/sites/weduc/tmp/weduc/compilador/" 
            comando += usuario?.username + "/" + fName + "/weduc.sh"


            println "------>"
            println comando;
            println "------>"
            
            def retorno;

            try {

		System.out.println("Iniciei a compilacao na linguagem " + linguagem.id);
                retorno = CommandShellToString.execute(comando);
		 System.out.println(retorno);
                
		 System.out.println("Finalizei a compilacao na linguagem " + linguagem.id);

            }
            catch (IOException ex) {
                System.out.println("Erro ao compilar o programa.");
            }
            catch (Exception e) {
            	println e
            }

            
            if(!(retorno.find("erro")||retorno.find("Erro")|| retorno.find("Error: "))) {
                programa.compilacoesBemSucedidas = programa.compilacoesBemSucedidas + 1;
                programa.save(flush: true)
                render "Compilação bem sucedida!"
            } else {
                programa.compilacoesMalSucedidas = programa.compilacoesMalSucedidas + 1;
                programa.save(flush: true)
                render "Ocorreram erros durante a compilação: \n" + retorno; 
            }
            
	}
	
	}//Termina compilar programa!

    def categorizarErros(String errorType, String usedStructures, String tipo, String descricao) {
        def usuario = springSecurityService.getCurrentUser()
        if (errorType.contains(descricao)) {
            def error = new Erro()
            error.usuario = usuario
            error.data = new Date()
            error.tipo = tipo
            error.quant = 1
            error.save(flush: true)
        }
        else if (usedStructures.contains(tipo)) {
            def error = new Erro()
            error.usuario = usuario
            error.data = new Date()
            error.tipo = tipo
            error.quant = 0
            error.save(flush: true)
        }
    }    
    
    //enviarCliente
    @Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
    def enviarCliente() {
	// Define usuário atual
        def usuario = springSecurityService.getCurrentUser()

        // Define a linguagem
        def linguagem = Linguagem.get(params.linguagem)

        // Define se é R-Educ ou não
        def reduc = params.reduc == "1" ? true : false

        // Procura um programa de mesmo nome;
        // caso não exista, o cria
        def programa = Programa.findWhere(
            usuario: usuario, 
            linguagem: linguagem,
            reduc: reduc,
            nome: params.nome
        )
        
        String fName = programa.nome;
        
        def programaCompilado = linguagem.sentExtension
        programaCompilado = programaCompilado.replace("nomedoprograma",  params.nome)
        
        File fDell = new File("/data/sites/weduc/tmp/weduc/envio/" + usuario?.username + "/")
	fDell.deleteDir()
        
       
        File d = new File("/data/sites/weduc/tmp/weduc/envio/" + usuario?.username)
        d.mkdirs()
        File weducClient = new File("/data/sites/weduc/tmp/weduc/envio/"+ usuario?.username +"/WeducClient.java")
       
        def origem = "/data/sites/weduc/weduc/weducClient.zip"	
        def destino = "/data/sites/weduc/tmp/weduc/envio/" + usuario?.username + "/"     
        CommandShellToString.execute("unzip " + origem + " -d " + destino); 

        def comando = linguagem.sendCode
        comando = comando.replace("nomedoprograma",  params.nome)
        
        String enviar = ""
        
        try {
            def copiarArquivoEnvio = "cp /data/sites/weduc/weduc/arquivos-de-envio/" + linguagem.id + "/arquivo" 
            copiarArquivoEnvio += " /data/sites/weduc/tmp/weduc/envio/"+ usuario?.username

            System.out.println(CommandShellToString.execute(copiarArquivoEnvio))

            origem = "/data/sites/weduc/tmp/weduc/envio/" + usuario?.username + "/arquivo" 	
            destino = "/data/sites/weduc/tmp/weduc/envio/" + usuario?.username + "/"     
            CommandShellToString.execute("unzip " + origem + " -d " + destino);  

            def zipper = new Zipper();
            def arquivos = zipper.listarEntradasZip(new File("/data/sites/weduc/tmp/weduc/envio/"+ usuario?.username + "/arquivo"))
            System.out.println(arquivos)

            enviar = arquivos.toString()
            System.out.println(enviar)
            enviar = enviar.replace('[','')      
            enviar = enviar.replace(']','')
            enviar = enviar.replace(',','')
            System.out.println(enviar)
        }  
        catch (Exception e) {
                 println e
        }
                
        def copiarArquivoCompilado = "cp /data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName + "/" + programaCompilado 
        copiarArquivoCompilado += " /data/sites/weduc/tmp/weduc/envio/"+ usuario?.username
        System.out.println(CommandShellToString.execute(copiarArquivoCompilado))
        
        def codigo = "import javax.swing.JOptionPane; \n import java.io.*; \n"
            codigo += "public class WeducClient {\n "
            codigo += "public static void main(String[] args) { \n"
            //A utilização de Process p não funciona!
        //    codigo += "try {Process p = Runtime.getRuntime().exec(\"jar xf W-Educ.jar " + programaCompilado +" " + enviar + " jssc.jar\"); \n p.waitFor(); } catch(Exception ex){ \n } \n"
        codigo += "try {Runtime.getRuntime().exec(\"jar xf W-Educ.jar " + programaCompilado +" " + enviar + " jssc.jar\"); \n try{Thread.sleep(1000); \n } catch(Exception e){};  } catch(Exception ex){ \n } \n"
            codigo += "String comando = \" " + comando + "\"; \n"
            codigo += "if (comando.contains(\"porta\")) { \n String portName = (String)JOptionPane.showInputDialog(null, \"Selecione a porta em que seu dispositivo está conectado:\", \"W-Educ - Seletor de Portas\","
            codigo += "JOptionPane.QUESTION_MESSAGE, null,SerialPortList.getPortNames(),null); \n \n"
            codigo +=  "if (portName != null){ \n comando = comando.replace(\"porta\",  portName); \n}\n} \n"
            /*codigo += "String comando1 = \"bash ./script.sh\" ; \n"
            codigo += "try { PrintWriter w = new PrintWriter(\"script.sh\", \"utf-8\"); \n"
            codigo += "w.println( comando  ); \n"
            codigo += "w.close(); \n } catch(Exception e) {} \n"
            codigo += "try {Process p = Runtime.getRuntime().exec(comando1); try{p.waitFor();} catch(Exception e) {}} catch(IOException e){ } \n   \n"  
            */ 
            //Os arquivos não estão sendo apagados no windows 10.
            codigo += "try {Process p = Runtime.getRuntime().exec(comando); \n p.waitFor();} catch(Exception e) {} \n \n"
            codigo += "try{Runtime.getRuntime().exec(\"del "+ enviar + "\"); } catch(Exception e){ } \n"
            codigo += "try{Runtime.getRuntime().exec(\"del "+ programaCompilado + " jssc.jar\");} catch(Exception e){ } \n "
            codigo += "try{Runtime.getRuntime().exec(\"rm "+ enviar + "\"); } catch(Exception e){ } \n"
            codigo += "try{Runtime.getRuntime().exec(\"rm "+ programaCompilado + " jssc.jar\"); } catch(Exception e){ } \n } \n}"
        weducClient << codigo
       
        //Compilação e geração do jar
        def retorno = CommandShellToString.execute("cd /data/sites/weduc/tmp/weduc/envio/" + usuario?.username + "&& javac *.java -classpath jssc.jar");
        CommandShellToString.execute("cd /data/sites/weduc/tmp/weduc/envio/" + usuario?.username + "&& jar cfm W-Educ.jar manifest.mf jssc.jar *.class " + enviar + " " +  programaCompilado)    
        System.out.println(retorno);
        
        File f = new File("/data/sites/weduc/tmp/weduc/envio/" + usuario?.username + "/W-Educ.jar" )
        
        FileInputStream fWEduc = new FileInputStream(f)
        response.setContentType("application/octet-stream")
        response.setHeader("Content-disposition", "filename= W-Educ.jar")
        response.outputStream << fWEduc
        
       // render "Programa enviado com sucesso."
    }
    

    //baixarPrograma    
    @Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
    def baixarPrograma() {
        // Define usuário atual
        def usuario = springSecurityService.getCurrentUser()

        // Define a linguagem
        def linguagem = Linguagem.get(params.linguagem)

        // Define se é R-Educ ou não
        def reduc = params.reduc == "1" ? true : false

        // Procura um programa de mesmo nome;
        // caso não exista, o cria
        def programa = Programa.findWhere(
            usuario: usuario, 
            linguagem: linguagem,
            reduc: reduc,
            nome: params.nome
        )
        
        if (programa == null){
            render "É necessário salvar o programa antes de baixá-lo."
            return
        }

	def extension
	if (!reduc){
		extension = linguagem.extension
	}
	else{
		extension = "rob"
	} 
        
        File c = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username)
	if (!c.exists()) {
            c.mkdirs()
            }
        
	//Procura se já existe o arquivo, delete e gera novamente para fazer download
	File fDelete = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + programa.nome + "." + extension)
	fDelete.delete()
	
	File f = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + programa.nome + "." + extension)
        f << programa.codigo //.replaceAll("\n", "\r\n");

        FileInputStream fPrograma = new FileInputStream(f)
        response.setContentType("application/octet-stream")
        response.setHeader("Content-disposition", "filename=" + params.nome + "."+ extension)
        response.outputStream << fPrograma
        return
    }

    //listarProgramas    
    @Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
    def listarProgramas() {

        // Define usuário atual
        def usuario = springSecurityService.getCurrentUser()

        // Define a linguagem
        def linguagem = Linguagem.get(params.linguagem)

        // Define se é R-Educ ou não
        def reduc = params.reduc == "1" ? true : false

        // Procura um programa de mesmo nome;
        // caso não exista, o cria
        def programas = Programa.findAllWhere(
            usuario: usuario, 
            linguagem: linguagem,
            reduc: reduc
        )

        [programas: programas]
    }
    
    //listarFunções    
    @Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
    def listarFuncoes() {
        [linguagem: Linguagem.get(params.id)]
    }

    //abrirPrograma
    @Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
    def abrirPrograma() {
        // Procura pelo programa de id específico
        def programa = Programa.get(params.id)
        render programa.codigo
    }

     @Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
    def excluirPrograma(){
         def programa = Programa.get(params.id) 
         programa.delete(flush:true)   
    }

    //exportarPrograma
    @Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR', 'ROLE_ALUNO'])
    def exportarPrograma(){

        // Define usuário atual
        def usuario = springSecurityService.getCurrentUser()

        // Define a linguagem
        def linguagem = Linguagem.get(params.linguagem)

        // Define se é R-Educ ou não
        def reduc = params.reduc == "1" ? true : false

        // Procura um programa de mesmo nome;
        // caso não exista, o cria
        
        def programa = Programa.findWhere(
            usuario: usuario, 
            linguagem: linguagem,
            reduc: reduc,
            nome: params.nome
        )
        
        if (programa == null){
            render "NO"
            return
        }    
        
        //Apaga todos os arquivos da pasta da linguagem para realizar nova compilação
	File fDell = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username)
        fDell.deleteDir()

        // Salva programa em arquivo temporário
        String fName = programa.nome;
        File d = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username)
        d.mkdir()
        File f = new File("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName + "" + programa.extensao)
        f << programa.codigo.replaceAll("\n", "\r\n");

        // Compila o arquivo utilizando o compilador REduc
        
        // Define o local do programa, para utilização futura
        programa.local = fName + programa.extensao + "." + linguagem.extension

        def language;
        language = new Language();
        language.setId(linguagem.id);
        language.setName(linguagem.name);
        language.setDescription(linguagem.description);
        language.setRobot(linguagem.robot);
        language.setExtension(linguagem.extension);

        language.setCompileCode(linguagem.compileCode);
        language.setCompilerFile(linguagem.compilerFile);
        language.setSendCode(linguagem.sendCode);
        language.setSentExtension(linguagem.sentExtension);

        language.setHeader(linguagem.header);
        language.setFootnote(linguagem.footnote);

        language.setMainFunction(linguagem.mainFunction);
        language.setOtherFunctions(linguagem.otherFunctions);
        language.setCallFunction(linguagem.callFunction);

        // Tipos
        def tipos = Tipos.findWhere(id: linguagem.types.id)
        def types;
        types = new Types();
        types.setId(tipos.id);
        types.setName(tipos.name);
        types.setDeclareFalse(tipos.declareFalse);
        types.setDeclareTrue(tipos.declareTrue);
        types.setDeclareFloat(tipos.declareFloat);
        types.setDeclareString(tipos.declareString);
        types.setDeclareBoolean(tipos.declareBoolean);
        language.setTypes(types);

        // Funções
        def funcoes = Funcao.findAllWhere(linguagem: linguagem)

        def functions = new ArrayList();

        funcoes.each() {
            obj ->

            def function = new LFunction();

            function.setId(obj.id);
            function.setName(obj.name);
            function.setType(obj.type);
            function.setReturnType(obj.returnType);
            function.setQntParameters(obj.qntParameters);
            function.setCode(obj.code);
            function.setDescription(obj.description);
            function.setTypeAliases(obj.typeAliases);
            function.setImageURL(obj.imageURL);
            functions.add(function);
        }

        // Defines
        def defines = Definicao.findAllWhere(linguagem: linguagem)

        def defines2 = new ArrayList();

        defines.each() {
            obj ->

            def defines3 = new Defines();
            
            defines3.setId(obj.id);
            defines3.setName(obj.name);
            defines3.setType(obj.type);
            defines3.setText(obj.text);
            defines3.setAlreadyExists(obj.alreadyExists);
            defines2.add(defines3);
        }

        // Operadores
        def operadores = Operadores.findWhere(id: linguagem.operators.id)
        def operators;
        operators = new Operators();
        operators.setId(operadores.id);
        operators.setEqualTo(operadores.equalTo);
        operators.setNotEqualTo(operadores.notEqualTo);
        operators.setGreaterThan(operadores.greaterThan);
        operators.setLessThan(operadores.lessThan);
        operators.setLessThanOrEqualTo(operadores.lessThanOrEqualTo);
        operators.setGreaterThanOrEqualTo(operadores.greaterThanOrEqualTo);
        operators.setLogicalAnd(operadores.logicalAnd);
        operators.setLogicalOr(operadores.logicalOr);
        operators.setLogicalNot(operadores.logicalNot);
        operators.setName(operadores.name);

        // Controle de fluxo
        def controleDeFluxo = ControleDeFluxo.findWhere(id: linguagem.controlFlow.id)
        ControlFlow controlFlow;
        controlFlow = new ControlFlow();
        controlFlow.setId(controleDeFluxo.id);
        controlFlow.setLanguageName(controleDeFluxo.languageName);
        controlFlow.setBreakCode(controleDeFluxo.breakCode);
        controlFlow.setDoCode(controleDeFluxo.doCode);
        controlFlow.setForCode(controleDeFluxo.forCode);
        controlFlow.setIfCode(controleDeFluxo.ifCode);
        controlFlow.setRepeatCode(controleDeFluxo.repeatCode);
        controlFlow.setSwitchCode(controleDeFluxo.switchCode);
        controlFlow.setWhileCode(controleDeFluxo.whileCode);

        def lexico = new analisadorLexico();
        lexico.readFile(f.path);
        def sintatico = new analisadorSintatico(lexico, "/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName, programa.extensao, linguagem.name, language.extension);
        sintatico.getMapeamento().defineValues(language, types, functions, operators, controlFlow, defines2);
        
        try{


            sintatico.startCompile();
            sintatico.closeFile();

        }
        catch (Exception e) {
            render "Erro ao compilar o programa. Consulte o administrador do sistema! \n"
            return;    
        }

            
        if(!sintatico.isError()) {
            programa.compilacoesBemSucedidas = programa.compilacoesBemSucedidas + 1;
            programa.save(flush: true)
            
            // Abre o arquivo compilado
            FileInputStream fPrograma = new FileInputStream("/data/sites/weduc/tmp/weduc/compilador/" + usuario?.username + "/" + fName + programa.extensao + "." + linguagem.extension)

            response.setContentType("application/octet-stream")
            // response.setHeader("Content-disposition", "filename=cv3")
            response.outputStream << fPrograma
            return
        } else {
            programa.compilacoesMalSucedidas = programa.compilacoesMalSucedidas + 1;
            programa.save(flush: true)
            render "Linha: " + sintatico.getErrorInt() + " Erro: " + sintatico.getErrorStr();
        }

    }
   

}
