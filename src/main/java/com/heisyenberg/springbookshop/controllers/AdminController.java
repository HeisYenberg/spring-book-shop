package com.heisyenberg.springbookshop.controllers;

import com.heisyenberg.springbookshop.dtos.BookDTO;
import com.heisyenberg.springbookshop.models.Book;
import com.heisyenberg.springbookshop.services.BooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class AdminController {
    private final BooksService booksService;

    @Autowired
    public AdminController(BooksService booksService) {
        this.booksService = booksService;
    }

    @GetMapping("/addBook")
    public String showAddBookPage(@ModelAttribute("bookDTO") BookDTO bookDTO) {
        return "add-book";
    }

    @PostMapping("/addBook")
    public String addBook(@Valid @ModelAttribute("bookDTO") BookDTO bookDTO,
                          BindingResult result) {
        if (bookDTO.getImageFile().isEmpty()) {
            result.addError(new FieldError("bookDTO", "imageFile",
                    "Изображение не было загружено"));
        }
        if (result.hasErrors()) {
            return "add-book";
        }
        bookDTO.loadImageFile();
        Book book = new Book(bookDTO);
        booksService.saveBook(book);
        return "redirect:/books";
    }

    @GetMapping("/editBook/{bookId}")
    public String showEditBookPage(@PathVariable("bookId") Long bookId,
                                   Model model) {
        Book book = booksService.getBook(bookId);
        model.addAttribute("bookDTO", new BookDTO(book));
        return "edit-book";
    }

    @PostMapping("/editBook/{bookId}")
    public String editBookPage(@PathVariable("bookId") Long bookId,
                               @Valid @ModelAttribute("bookDTO") BookDTO bookDTO,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "edit-book";
        }
        if (!bookDTO.getImageFile().isEmpty()) {
            bookDTO.reUploadImageFile();
        }
        Book book = new Book(bookDTO);
        book.setId(bookId);
        booksService.saveBook(book);
        return "redirect:/books";
    }

    @PostMapping("/deleteBook/{bookId}")
    public String deleteBook(@PathVariable("bookId") Long bookId,
                             RedirectAttributes redirectAttributes) {
        try {
            booksService.deleteBook(bookId);
            redirectAttributes.addFlashAttribute("successMessage", "Удалено");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Невозможно удалить, так как находится в корзине");
        }
        return "redirect:/books";
    }
}
