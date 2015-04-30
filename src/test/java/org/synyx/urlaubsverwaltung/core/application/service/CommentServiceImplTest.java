package org.synyx.urlaubsverwaltung.core.application.service;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.dao.CommentDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Optional;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.application.service.CommentServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CommentServiceImplTest {

    private CommentService commentService;

    private CommentDAO commentDAO;

    @Before
    public void setUp() {

        commentDAO = Mockito.mock(CommentDAO.class);
        commentService = new CommentServiceImpl(commentDAO);
    }


    @Test
    public void ensureCreatesACommentAndPersistsIt() {

        Person author = new Person();
        Application application = new Application();

        Comment comment = commentService.create(application, ApplicationStatus.ALLOWED, Optional.<String>empty(),
                author);

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertNotNull("Date should be set", comment.getDate());
        Assert.assertNotNull("Status should be set", comment.getStatus());
        Assert.assertNotNull("Author should be set", comment.getPerson());
        Assert.assertNotNull("Application for leave should be set", comment.getApplication());

        Assert.assertEquals("Wrong status", ApplicationStatus.ALLOWED, comment.getStatus());
        Assert.assertEquals("Wrong author", author, comment.getPerson());

        Assert.assertNull("Text should not be set", comment.getText());

        Mockito.verify(commentDAO).save(Mockito.eq(comment));
    }


    @Test
    public void ensureCreationOfCommentWithTextWorks() {

        Person author = new Person();
        Application application = new Application();

        Comment comment = commentService.create(application, ApplicationStatus.REJECTED, Optional.of("Foo"), author);

        Assert.assertNotNull("Should not be null", comment);

        Assert.assertNotNull("Date should be set", comment.getDate());
        Assert.assertNotNull("Status should be set", comment.getStatus());
        Assert.assertNotNull("Author should be set", comment.getPerson());
        Assert.assertNotNull("Text should be set", comment.getText());

        Assert.assertEquals("Wrong status", ApplicationStatus.REJECTED, comment.getStatus());
        Assert.assertEquals("Wrong author", author, comment.getPerson());
        Assert.assertEquals("Wrong text", "Foo", comment.getText());

        Mockito.verify(commentDAO).save(Mockito.eq(comment));
    }
}
