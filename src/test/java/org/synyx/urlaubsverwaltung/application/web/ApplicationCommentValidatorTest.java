package org.synyx.urlaubsverwaltung.application.web;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;


/**
 * Unit test for {@link ApplicationCommentValidator}.
 */
public class ApplicationCommentValidatorTest {

    private ApplicationCommentValidator validator;
    private Errors errors;

    @Before
    public void setUp() {

        validator = new ApplicationCommentValidator();
        errors = mock(Errors.class);
    }


    @Test
    public void ensureSupportsCommentClass() {

        assertTrue(validator.supports(ApplicationCommentForm.class));
    }


    @Test
    public void ensureDoesNotSupportNull() {

        assertFalse(validator.supports(null));
    }


    @Test
    public void ensureDoesNotSupportOtherClass() {

        assertFalse(validator.supports(SickNoteComment.class));
    }


    @Test
    public void ensureReasonCanBeNullIfNotMandatory() {

        ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(false);
        comment.setText(null);

        validator.validate(comment, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureReasonCanBeEmptyIfNotMandatory() {

        ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(false);
        comment.setText("");

        validator.validate(comment, errors);

        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureReasonCanNotBeNullIfMandatory() {

        ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(true);
        comment.setText(null);

        validator.validate(comment, errors);

        verify(errors).rejectValue("text", "error.entry.mandatory");
    }


    @Test
    public void ensureReasonCanNotBeEmptyIfMandatory() {

        ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(true);
        comment.setText("");

        validator.validate(comment, errors);

        verify(errors).rejectValue("text", "error.entry.mandatory");
    }


    @Test
    public void ensureThereIsAMaximumCharLengthForReason() {

        ApplicationCommentForm comment = new ApplicationCommentForm();

        comment.setText(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
            + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
            + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(comment, errors);

        verify(errors).rejectValue("text", "error.entry.tooManyChars");
    }
}
