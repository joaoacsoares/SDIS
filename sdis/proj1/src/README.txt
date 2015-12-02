SDIS - 2014/2015
Projeto desenvolvido no âmbito da unidade curricular Sistemas Distribuídos
3º ano, 2º Semestre - Mestrado Integrado em Engenharia Informática e Computação
João António Soares | Mafalda Falcão
Faculdade de Engenharia da Universidade do Porto

Execução:

java main/Main <MCAddr> <MCPort> <MDBAddr> <MDBPort> <MDRAddr> <MDRPort> para executar a aplicação p.ex.: <231.0.0.1> <2002> <231.0.0.2> <2002> <231.0.0.3> <2002>


Faltou implementar protocolo de "Space Reclaiming" devido a um planeamento de trabalho sub-óptimo, o que levou a vários atrasos no desenvolvimento.

Para executar os subprotocolos:
- Backup: Escolher a opção de Backup na GUI (1); inserir o nome do ficheiro escolhido (podemos pôr o path completo do ficheiro mas, por defeito, procura o ficheiro
na pasta bin do projeto); inserir parâmetros adicionais (versão e replication degree).
- Restore:  Escolher a opção de Restore na GUI (2); inserir o nome do ficheiro escolhido (podemos pôr o path completo do ficheiro mas, por defeito, procura o ficheiro
na pasta bin do projeto); 
- Delete:  Escolher a opção de Delete na GUI (3); inserir o nome do ficheiro escolhido (podemos pôr o path completo do ficheiro mas, por defeito, procura o ficheiro
na pasta bin do projeto).