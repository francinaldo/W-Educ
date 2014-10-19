package org.natalnet.weduc

import grails.plugin.springsecurity.annotation.Secured

class FuncaoController {

        def springSecurityService

	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR'])
	def index() {
		redirect action: "listar"
	}

	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR'])
	def nova() {}

	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR'])
	def salvar() {


        // Fim do cadastro de linguagem

        if(linguagem.id != null) {
        	flash.message = "Linguagem " + linguagem.name + " cadastrada com sucesso."
        	redirect action: "editar", id: linguagem.id
        } else {
        	flash.message = "Erro ao cadastrar a linguagem " + linguagem.name + "."
        	redirect action: "nova" //, params: params
        }

	}

	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR'])
	def editar() {
		def linguagem = Linguagem.get(params.id)
		[linguagem: linguagem]
	}

	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR'])
	def atualizar() {

	
        
        println "Cadastro da linguagem " + linguagem.name + " atualizado por " + springSecurityService.getCurrentUser()

        // Fim do cadastro de linguagem

        if(linguagem.id != null) {
        	flash.message = "Linguagem " + linguagem.name + " atualizada com sucesso."
        	redirect action: "editar", id: linguagem.id
        } else {
        	flash.message = "Erro ao atualizar a linguagem " + linguagem.name + "."
        	redirect action: "editar" //, params: params
        }

	}

	@Secured(['ROLE_ADMIN', 'ROLE_PROFESSOR'])
	def listar() {
		def usuario = springSecurityService.getCurrentUser()
		[linguagens: Linguagem.findAllByAutor(usuario)]
	}
}