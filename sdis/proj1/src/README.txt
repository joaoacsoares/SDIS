SDIS - 2014/2015
Projeto desenvolvido no �mbito da unidade curricular Sistemas Distribu�dos
3� ano, 2� Semestre - Mestrado Integrado em Engenharia Inform�tica e Computa��o
Jo�o Ant�nio Soares | Mafalda Falc�o
Faculdade de Engenharia da Universidade do Porto

Execu��o:

java main/Main <MCAddr> <MCPort> <MDBAddr> <MDBPort> <MDRAddr> <MDRPort> para executar a aplica��o p.ex.: <231.0.0.1> <2002> <231.0.0.2> <2002> <231.0.0.3> <2002>


Faltou implementar protocolo de "Space Reclaiming" devido a um planeamento de trabalho sub-�ptimo, o que levou a v�rios atrasos no desenvolvimento.

Para executar os subprotocolos:
- Backup: Escolher a op��o de Backup na GUI (1); inserir o nome do ficheiro escolhido (podemos p�r o path completo do ficheiro mas, por defeito, procura o ficheiro
na pasta bin do projeto); inserir par�metros adicionais (vers�o e replication degree).
- Restore:  Escolher a op��o de Restore na GUI (2); inserir o nome do ficheiro escolhido (podemos p�r o path completo do ficheiro mas, por defeito, procura o ficheiro
na pasta bin do projeto); 
- Delete:  Escolher a op��o de Delete na GUI (3); inserir o nome do ficheiro escolhido (podemos p�r o path completo do ficheiro mas, por defeito, procura o ficheiro
na pasta bin do projeto).