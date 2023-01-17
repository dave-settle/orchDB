/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra.web;

/**
 * Controls the listing concerts, and provides means to load a concert via a
 * text file (or a ZIP file containing multiple text files)
 *
 * @author dave.settle@osinet.co.uk on 20 Aug 2022
 */
import com.banburysymphony.orchestra.data.Artist;
import com.banburysymphony.orchestra.data.Concert;
import com.banburysymphony.orchestra.data.Engagement;
import com.banburysymphony.orchestra.data.Piece;
import com.banburysymphony.orchestra.data.Venue;
import com.banburysymphony.orchestra.jpa.ArtistRepository;
import com.banburysymphony.orchestra.jpa.ConcertRepository;
import com.banburysymphony.orchestra.jpa.EngagementRepository;
import com.banburysymphony.orchestra.jpa.PieceRepository;
import com.banburysymphony.orchestra.jpa.VenueRepository;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller // This means that this class is a web Controller
@RequestMapping(path = "/concert")
@ConfigurationProperties(prefix = "bso.concert")
@ConfigurationPropertiesScan
public class ConcertController {

    @Autowired
    ConcertRepository concertRepository;

    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    PieceRepository pieceRepository;

    @Autowired
    VenueRepository venueRepository;

    @Autowired
    EngagementRepository engagementRepository;
    /**
     * Hopefully people will start using composer names with accents, but some
     * composers are commonly recorded without accents
     */
    Map<String, String> composerMap;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private String encoding = "UTF-8";
    
    private String basedir = "/files/";

    private static final Logger log = LoggerFactory.getLogger(ConcertController.class);

    public ConcertController() {
        this.composerMap = Map.ofEntries(
                new AbstractMap.SimpleEntry<>("dvorak", "Dvořák"),
                new AbstractMap.SimpleEntry<>("bartok", "Bartók"),
                new AbstractMap.SimpleEntry<>("faure", "Fauré"),
                new AbstractMap.SimpleEntry<>("francaix", "Françaix")
        );
    }

    /**
     * The login method just redirects to the concert listing page. The actual
     * login operation is done automatically by the security layer
     *
     * @param model
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        return "redirect:/concert/list";
    }

    protected Sort sortOrder() {
        return Sort.by(Direction.DESC, "date");
    }
    /**
     * List all of the concerts
     *
     * @param model
     * @return
     */
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String listConcerts(Model model) {
        log.info("Listing all concerts");
        Iterable<Concert> concerts = concertRepository.findAll(sortOrder());
        model.addAttribute("concerts", filter(concerts));
        return "listConcerts";
    }

    @RequestMapping(path = "/listByPiece", method = RequestMethod.GET)
    public String listConcertsByPiece(Model model, @RequestParam(name = "id", required = true) int id) {
        log.info("Listing all concerts with piece " + id);
        Optional<Piece> piece = pieceRepository.findById(id);
        if (piece.isEmpty()) {
            throw new NoSuchElementException("Piece " + id + " not found");
        }
        List<Concert> concerts = concertRepository.findAllByPieces(piece.get(), sortOrder());
        model.addAttribute("concerts", filter(concerts));
        return "listConcerts";
    }

    @RequestMapping(path = "/listById", method = RequestMethod.GET)
    public String listConcertsById(Model model, @RequestParam(name = "id", required = true) int id) {
        log.info("Listing all concerts with id " + id);
        Optional<Concert> c = concertRepository.findById(id);
        List<Concert> concerts = new LinkedList<>();
        if (c.isPresent()) {
            concerts.add(c.get());
        }
        model.addAttribute("concerts", filter(concerts));
        return "listConcerts";
    }

    @RequestMapping(path = "/listByConductor", method = RequestMethod.GET)
    public String listConcertsByConductor(Model model, @RequestParam(name = "id", required = true) int id) {
        log.info("Listing all concerts for conductor ID " + id);
        Optional<Artist> conductor = artistRepository.findById(id);
        List<Concert> concerts = concertRepository.findAllByConductor(conductor.get(), sortOrder());
        model.addAttribute("concerts", filter(concerts));
        return "listConcerts";
    }

    @RequestMapping(path = "/listByComposer", method = RequestMethod.GET)
    public String listConcertsByComposer(Model model, @RequestParam(name = "name", required = true) String name) {
        log.info("Listing all concerts containing composer " + name);
        List<Concert> concerts = concertRepository.findAllByComposer(name, sortOrder());
        model.addAttribute("concerts", filter(concerts));
        return "listConcerts";
    }

    @RequestMapping(path = "/listBySoloist", method = RequestMethod.GET)
    public String listConcertsBySoloist(Model model, @RequestParam(name = "id", required = true) Integer id) {
        log.info("Listing all concerts containing artist " + id);
        List<Concert> concerts = concertRepository.findAllBySoloist(id, sortOrder());
        model.addAttribute("concerts", filter(concerts));
        return "listConcerts";
    }

    @RequestMapping(path = "/listBySkill", method = RequestMethod.GET)
    public String listConcertsBySkill(Model model, @RequestParam(name = "name", required = true) String name) {
        log.info("Listing all concerts containing skill " + name);
        List<Concert> concerts = concertRepository.findAllBySkill(name, sortOrder());
        model.addAttribute("concerts", filter(concerts));
        return "listConcerts";
    }

    /**
     * Upload a simple text file
     *
     * @param file
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadConcert(@RequestParam("file") MultipartFile file) throws IOException, ParseException {
        log.debug("Received file upload");
        BufferedReader r = new BufferedReader(new InputStreamReader(file.getInputStream(), getEncoding()));
        loadConcertFile(r);
        return "redirect:/concert/list";
    }

    /**
     * Upload a zip file containing a number of concerts
     *
     * @param file
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @RequestMapping(path = "/uploadZip", method = RequestMethod.POST)
    public String uploadConcertZip(@RequestParam("file") MultipartFile file) throws IOException, ParseException {
        log.debug("Received zip file upload");
        ZipInputStream zis = new ZipInputStream(file.getInputStream());
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            log.debug("reading zip file " + entry.getName());
            BufferedReader r = new BufferedReader(new InputStreamReader(zis, getEncoding()));
            loadConcertFile(r);
        }
        return "redirect:/concert/list";
    }

    /**
     * Load a text file representing a concert
     *
     * @param r
     * @throws IOException
     * @throws ParseException
     */
    public void loadConcertFile(BufferedReader r) throws IOException, ParseException {

        String line;
        Venue venue = getVenue("Unknown");
        Artist conductor = getArtist("Unknown");
        Set<Engagement> soloists = new HashSet<>();
        List<Piece> pieces = new LinkedList<>();
        /*
         * Read the first line - BSO 2000-03-25 (Saturday)
         */
        line = getNextLine(r);
        StringTokenizer tok = new StringTokenizer(line, " ");
        tok.nextToken(); // Ignore first word   
        String concertDate = tok.nextToken();
        Date held = new Date(sdf.parse(concertDate).getTime());
        log.debug("parsed (" + concertDate + ") as " + DateFormat.getDateInstance().format(held));
        /*
         * Update: allow an existing concert to be re-loaded, to allow any errors to 
         * be corrected
         */
        Optional<Concert> current = concertRepository.findByDate(held);
        if (current.isPresent()) {
            log.warn("Replacing concert on " + concertDate);
            concertRepository.deleteById(current.get().getId());
        }
        while ((line = getNextLine(r)) != null) {
            log.debug("read: " + line);
            if (!line.contains(":")) {
                continue;
            }
            /*
             * There's at least one token
             */
            tok = new StringTokenizer(line, ":");
            String key = tok.nextToken().trim();
            String value = "";
            /*
             * And possibly two
             */
            if (tok.hasMoreTokens()) {
                value = tok.nextToken().trim();
            }
            if ("Venue".equalsIgnoreCase(key)) {
                venue = getVenue(value);
                venue = venueRepository.save(venue);
                log.debug("saved venue: " + venue);
                continue;
            }
            if ("Conductor".equalsIgnoreCase(key)) {
                conductor = getArtist(value);
                conductor = artistRepository.save(conductor);
                log.debug("saved conductor: " + conductor);
                continue;
            }
            /*
             * Soloist is actually a comma-separated list Also allow plural
             */
            if ("Soloist".equalsIgnoreCase(key) || "Soloists".equalsIgnoreCase(key)) {
                tok = new StringTokenizer(value, ",");
                while (tok.hasMoreTokens()) {
                    String s = tok.nextToken();  // e.g. Abi Stevens (flute)
                    StringTokenizer t = new StringTokenizer(s, "()");
                    String n = t.nextToken().trim();  // e.g. Abi Stevens
                    String skill = "";
                    if (t.hasMoreTokens()) {
                        skill = t.nextToken().trim(); // e.g. flute
                    }
                    log.debug("found soloist [" + n + "] with skill [" + skill + "]");
                    Artist a = getArtist(n);
                    a = artistRepository.save(a);
                    Engagement e = new Engagement(a, skill);
                    e = engagementRepository.save(e);
                    log.debug("saved engagement " + e);
                    soloists.add(e);
                }
                continue;
            }
            /*
             * The program lists the pieces that were played, followed by a
             * blank line
             */
            if ("Program".equalsIgnoreCase(key)) {
                while ((line = getNextLine(r)) != null) {
                    log.debug("read " + line);
                    if (!line.contains(":")) {
                        break;
                    }
                    tok = new StringTokenizer(line, ":");
                    String composer = tok.nextToken().trim();
                    /*
                     * Correct Dvorak :)
                     */
                    String fixedComposer = composerMap.get(composer.toLowerCase());
                    if (fixedComposer != null) {
                        log.debug("mapping " + composer + " to " + fixedComposer);
                        composer = fixedComposer;
                    }
                    String title = tok.nextToken().trim();
                    String subtitle = null;
                    /*
                     * The details part might have something in brackets
                     */
                    if (title.contains("(")) {
                        StringTokenizer st = new StringTokenizer(title, "()");
                        title = st.nextToken().trim();
                        subtitle = st.nextToken().trim();
                        /*
                         * If this is the name of a soloist, we can ignore it
                         */
                        for (Engagement e : soloists) {
                            if (e.getArtist().getName().equalsIgnoreCase(subtitle)) {
                                log.debug("subtitle [" + subtitle + "] is an artist - ignored");
                                subtitle = "";
                            }
                        }
                    }
                    /*
                     * Put everything together
                     */
                    Optional<Piece> p = pieceRepository.checkTitle(pieceRepository.findAllByComposerOrderByTitleAsc(composer), title);
                    Piece piece = p.orElse(new Piece(composer, title, subtitle));
                    log.debug("found " + piece);
                    piece = pieceRepository.save(piece);
                    pieces.add(piece);
                    log.debug("saved piece " + piece);
                }
            }
            /*
             * Update any existing record
             */
            Optional<Concert> c = concertRepository.findByDate(held);
            Concert concert = c.orElse(new Concert(venue, held, conductor));
            /*
             * Update the record if found
             */
            concert.setConductor(conductor);
            concert.setVenue(venue);
            concert.setSoloists(soloists);
            concert.setPieces(pieces);
            /*
             * Save the record
             */
            concert = concertRepository.save(concert);
        }
    }

    /**
     * Return the next line which isn't a comment
     *
     * @param r
     * @return
     */
    private String getNextLine(BufferedReader r) throws IOException {
        String line = r.readLine();
        while ((line != null) && (line.startsWith("#"))) {
            line = r.readLine();
        }
        return line;
    }

    /**
     * Delete a specific concert
     *
     * @param id
     * @param model
     * @return listVenues to list the remaining concerts
     */
    @RequestMapping(path = "/delete/{id}")
    public String deleteConcert(@PathVariable(name = "id", required = true) int id) {
        Optional<Concert> concert = concertRepository.findById(id);
        if (concert.isPresent()) {
            log.info("Deleting " + concert);
            concertRepository.delete(concert.get());
        }
        return ("redirect:/concert/list");
    }
    /**
     * Download the programme PDF file, held on disk
     * @param id the ID of the concert
     * @return the PDF of the concert programme
     * @throws IOException 
     */
    @RequestMapping(value = "/programme/{id}", method = RequestMethod.GET, produces = "application/pdf")
    public ResponseEntity<InputStreamResource> downloadPDFFile(@PathVariable(name = "id", required = true) int id)
            throws IOException {
        Optional<Concert> concert = concertRepository.findById(id);
        if(concert.isEmpty())
            throw new NoSuchElementException("concert " + id + " not found");
        String filename = findProgrammeFileName(concert.get());
        log.debug("concert id " + id + " maps to " + filename);
        ClassPathResource pdfFile = new ClassPathResource(filename);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(pdfFile.getFilename(), pdfFile.getFilename());
        return ResponseEntity
                .ok().cacheControl(CacheControl.noCache())
                .headers(headers)
                .contentLength(pdfFile.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(pdfFile.getInputStream()));
    }
    
    public String findProgrammeFileName(Concert concert) {
        String date = sdf.format(concert.getDate());
        return getBasedir() + "programme-" + date + ".pdf";
    }
 
    /**
     * Download the newspaper article file, held on disk
     * @param id the ID of the concert
     * @return the PDF of the concert programme
     * @throws IOException 
     */
    @RequestMapping(value = "/article/{id}", method = RequestMethod.GET, produces = "application/pdf")
    public ResponseEntity<InputStreamResource> downloadArticleFile(@PathVariable(name = "id", required = true) int id)
            throws IOException {
        Optional<Concert> concert = concertRepository.findById(id);
        if(concert.isEmpty())
            throw new NoSuchElementException("concert " + id + " not found");
        String filename = findArticleFileName(concert.get());
        log.debug("concert id " + id + " maps to " + filename);
        ClassPathResource pdfFile = new ClassPathResource(filename);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(pdfFile.getFilename(), pdfFile.getFilename());
        return ResponseEntity
                .ok().cacheControl(CacheControl.noCache())
                .headers(headers)
                .contentLength(pdfFile.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(pdfFile.getInputStream()));
    }
    public String findArticleFileName(Concert concert) {
        String date = sdf.format(concert.getDate());
        return getBasedir() + "programme-" + date + "-article.pdf";
    }
    
    /**
     * Return the concert information as a text file, in case people want to
     * edit the information
     * @param id
     * @return
     * @throws IOException 
     */
    @GetMapping(value = "/file/{id}", produces = "text/plain; charset=UTF-8")
    public ResponseEntity<InputStreamResource> downloadConcertFile(@PathVariable(name = "id", required = true) int id)
            throws IOException {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> {return new NoSuchElementException("concert " + id + " not found");});
        String filename = "programme-" + sdf.format(concert.getDate()) + ".txt";
        log.debug("create concert file " + filename);
        File tempFile = File.createTempFile("concert", ".txt");
        tempFile.deleteOnExit();
        BufferedWriter outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), encoding));
        dump(concert, outputFile);
        outputFile.close();
        /*
         * Send the information to the user
         */
        InputStream r = new FileInputStream(tempFile);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(filename, filename);
        return ResponseEntity
                .ok().cacheControl(CacheControl.noCache())
                .headers(headers)
                .contentLength(tempFile.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(r));
    }
    /**
     * Reverse the parse operation and output a text representation of the concert
     * on the writer
     * @param concert
     * @param out
     * @throws IOException 
     */
    protected void dump(Concert concert, BufferedWriter out) throws IOException {
        out.append("BSO " + sdf.format(concert.getDate()));
        out.newLine();  out.newLine();
        out.append("Venue: " + concert.getVenue().getName());
        out.newLine();  out.newLine();
        out.append("Conductor: " + concert.getConductor().getName());
        out.newLine();  out.newLine();
        out.append("Soloist: ");
        for(Iterator<Engagement> it = concert.getSoloists().iterator(); it.hasNext(); ) {
            Engagement e = it.next();
            out.append(e.getArtist().getName() + " (" + e.getSkill() + ")");
            if(it.hasNext())
                out.append(", ");
        }
        out.newLine();  out.newLine();
        out.append("Program: "); out.newLine();
        for(Piece p: concert.getPieces()) {
            out.append("\t" + p.getComposer() + ": \t" + p.getTitle());
            out.newLine();
        }
        out.newLine();  out.newLine();
    }
    
    /**
     * Fetch the artist with the given name, or a new artist if they don't exist
     *
     * @param name
     * @return
     */
    protected Artist getArtist(String name) {
        Optional<Artist> c = artistRepository.findByName(name);
        return c.orElse(new Artist(name));
    }

    protected Venue getVenue(String name) {
        Optional<Venue> c = venueRepository.findByName(name);
        return c.orElse(new Venue(name));
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return the basedir
     */
    public String getBasedir() {
        return basedir;
    }

    /**
     * @param basedir the basedir to set
     */
    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }
    /**
     * As we are now planning future concerts, the list of concerts returned
     * by this controller should be restricted to concerts which have already
     * happened
     * @param concerts a list of all concerts
     * @return concerts whose date is in the past
     */
    protected List<Concert> filter(Iterable<Concert> concerts) {
        Date now = new Date(System.currentTimeMillis());
        Predicate<Concert> byDate = concert -> concert.getDate().before(now);
        return StreamSupport.stream(concerts.spliterator(), false).filter(byDate).collect(Collectors.toList());
    }
}
