package org.fpij.jitakyoei.model.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.fpij.jitakyoei.model.beans.Aluno;
import org.fpij.jitakyoei.model.beans.Endereco;
import org.fpij.jitakyoei.model.beans.Entidade;
import org.fpij.jitakyoei.model.beans.Filiado;
import org.fpij.jitakyoei.model.beans.Professor;
import org.fpij.jitakyoei.model.validator.AlunoValidator;
import org.fpij.jitakyoei.model.validator.Validator;
import org.fpij.jitakyoei.util.DatabaseManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.GenerateObjects;

public class DaoImplTest {
	
	private static DAO<Aluno> alunoDao;
	private static Aluno aluno;
	private static Entidade entidade;
	private static Endereco endereco;
	private static Filiado f1;
	private static Filiado filiadoProf;
	private static Professor professor;
	
	@BeforeClass
	public static void setUp(){
		DatabaseManager.setEnviroment(DatabaseManager.TEST);
		f1 = new Filiado();
		f1.setNome("Aécio");
		f1.setCpf("036.464.453-27");
		f1.setDataNascimento(new Date());
		f1.setDataCadastro(new Date());
		f1.setId(1332L);
		
		endereco = new Endereco();
		endereco.setBairro("Dirceu");
		endereco.setCep("64078-213");
		endereco.setCidade("Teresina");
		endereco.setEstado("PI");
		endereco.setRua("Rua Des. Berilo Mota");
		
		filiadoProf = new Filiado();
		filiadoProf.setNome("Professor");
		filiadoProf.setCpf("036.464.453-27");
		filiadoProf.setDataNascimento(new Date());
		filiadoProf.setDataCadastro(new Date());
		filiadoProf.setId(3332L);
		filiadoProf.setEndereco(endereco);
		
		professor = new Professor();
		professor.setFiliado(filiadoProf);
		
		entidade = new Entidade();
		entidade.setEndereco(endereco);
		entidade.setNome("Academia 1");
		entidade.setTelefone1("(086)1234-5432");
		
		aluno = new Aluno();
		aluno.setFiliado(f1);
		aluno.setProfessor(professor);
		aluno.setEntidade(entidade);
		
		alunoDao = new DAOImpl<Aluno>(Aluno.class);
	}

	public static void clearDatabase(){
		List<Aluno> all = alunoDao.list();
		for (Aluno each : all) {
			alunoDao.delete(each);
		}
		assertEquals(0, alunoDao.list().size());
	}

	// check differents constructors
	@Test
	public void checkConstructorWithUseEquals(){
		DAO<Aluno> alunoDao = new DAOImpl<Aluno>(Aluno.class, false );

		assertNotNull(alunoDao);
	}

	@Test
	public void checkConstructorWithUseEqualsAndValidatorCustom(){

		DAO<Aluno> alunoDao = new DAOImpl<Aluno>(
				Aluno.class,
				new AlunoValidator(),
				false);

		assertNotNull(alunoDao);
	}

	// test save function
	@Test
	public void  testSalvarAlunoComAssociassoes() throws Exception{
		clearDatabase();

		boolean returnReceived = alunoDao.save(aluno);
		assertEquals("036.464.453-27", alunoDao.get(aluno).getFiliado().getCpf());
		assertEquals("Aécio", alunoDao.get(aluno).getFiliado().getNome());
		assertEquals("Professor", alunoDao.get(aluno).getProfessor().getFiliado().getNome());
		assertEquals("Dirceu", alunoDao.get(aluno).getProfessor().getFiliado().getEndereco().getBairro());
		assertEquals(true, returnReceived);

	}

	@Test
	public void  testSalvarAlunoComErroValidacao(){
		clearDatabase();

		class CustomValidator<T> implements Validator<T> {
			@Override
			public boolean validate(T obj) {
				return false;
			}
		}

		DAO<Aluno> alunoDao = new DAOImpl<Aluno>(
				Aluno.class,
				new CustomValidator<Aluno>(),
				false);


		boolean returnReceived = alunoDao.save(aluno);
		assertEquals(false, returnReceived);

	}

	
	@Test
	public void updateAluno() throws Exception{
		clearDatabase();
		assertEquals(0, alunoDao.list().size());
		
		alunoDao.save(aluno);
		assertEquals(1, alunoDao.list().size());
		assertEquals("Aécio", aluno.getFiliado().getNome());
		
		Aluno a1 = alunoDao.get(aluno);
		a1.getFiliado().setNome("TesteUpdate");
		alunoDao.save(a1);
		
		Aluno a2 = alunoDao.get(a1);
		assertEquals("TesteUpdate", a2.getFiliado().getNome());
		assertEquals(1, alunoDao.list().size());
	}

	// test list functions
	@Test
	public void testgetAlunosWithUseEquals(){
		int qtd = alunoDao.list().size();

		DAO<Aluno> alunoDaoTest = new DAOImpl<>(Aluno.class, true);

		alunoDaoTest.save(aluno);
		assertEquals(qtd+1, alunoDaoTest.list().size());


		Aluno retornoAluno = alunoDaoTest.get(aluno);

		assertEquals(aluno, retornoAluno);
	}

	@Test
	public void testGetAlunosWithUseEqualsAndEmpty(){
		DAO<Aluno> alunoDaoTest = new DAOImpl<>(Aluno.class, true);

		try {
			Aluno retornoAluno = alunoDaoTest.get(aluno);
		} catch (Exception e){
			assertNotNull(e);
			assertEquals(e.getClass(), IllegalArgumentException.class);

		}
	}

	// test get function

	@Test
	public void testListarEAdicionarAlunos(){
		int qtd = alunoDao.list().size();

		alunoDao.save(GenerateObjects.generateAluno());
		assertEquals(qtd + 1, alunoDao.list().size());

		alunoDao.save(GenerateObjects.generateAluno());
		assertEquals(qtd + 2, alunoDao.list().size());

		alunoDao.save(GenerateObjects.generateAluno());
		assertEquals(qtd + 3, alunoDao.list().size());

		alunoDao.save(GenerateObjects.generateAluno());
		assertEquals(qtd + 4, alunoDao.list().size());

		clearDatabase();
		assertEquals(0, alunoDao.list().size());

		alunoDao.save(GenerateObjects.generateAluno());
		assertEquals(1, alunoDao.list().size());
	}
	@Test
	public void testSearchAluno() throws Exception{
		clearDatabase();
		alunoDao.save(aluno);
		
		Filiado f = new Filiado();
		f.setNome("Aécio");
		Aluno a = new Aluno();
		a.setFiliado(f);
		
		List<Aluno> result = alunoDao.search(a);
		assertEquals(1, result.size());
		assertEquals("036.464.453-27", result.get(0).getFiliado().getCpf());
		
		clearDatabase();
		assertEquals(0, alunoDao.search(a).size());
	}
	
	@AfterClass
	public static void closeDatabase(){
		clearDatabase();
		DatabaseManager.close();
	}
	
}
