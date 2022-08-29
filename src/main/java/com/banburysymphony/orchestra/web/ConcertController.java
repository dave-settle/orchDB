/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banburysymphony.orchestra.web;

/**
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    private String encoding = "UTF-8";

    private static final Logger log = LoggerFactory.getLogger(ConcertController.class);
    /**
     * The login method just redirects to the concert listing page.
     * The actual login operation is done automatically by the security layer
     * @param model
     * @return 
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        return "redirect:/concert/list";
    }
    /**
     * List all of the concerts
     * @param model
     * @return 
     */
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String listConcerts(Model model) {
        log.info("Listing all concerts");
        Iterable<Concert> concerts = concertRepository.findAll(Sort.by("date"));
        model.addAttribute("concerts", concerts);
        return "listConcerts";
    }
    @RequestMapping(path = "/listByPiece", method = RequestMethod.GET)
    public String listConcertsByPiece(Model model, @RequestParam(name = "id", required = true) int id) {
        log.info("Listing all concerts with piece " + id);
        Optional<Piece> piece = pieceRepository.findById(id);
        List<Concert> concerts = concertRepository.findAllByPieces(piece.get(), Sort.by("date"));
        model.addAttribute("concerts", concerts);
        return "listConcerts";
    }
    @RequestMapping(path = "/listById", method = RequestMethod.GET)
    public String listConcertsById(Model model, @RequestParam(name = "id", required = true) int id) {
        log.info("Listing all concerts with id " + id);
        Optional<Concert> c = concertRepository.findById(id);
        List<Concert> concerts = new LinkedList<>();
        if(c.isPresent())
                concerts.add(c.get());
        model.addAttribute("concerts", concerts);
        return "listConcerts";
    }
    @RequestMapping(path = "/listByConductor", method = RequestMethod.GET)
    public String listConcertsByConductor(Model model, @RequestParam(name = "id", required = true) int id) {
        log.info("Listing all concerts for conductor ID " + id);
        Optional<Artist> conductor = artistRepository.findById(id);
        List<Concert> concerts = concertRepository.findAllByConductor(conductor.get(), Sort.by("date"));
        model.addAttribute("concerts", concerts);
        return "listConcerts";
    }
    @RequestMapping(path = "/listByComposer", method = RequestMethod.GET)
    public String listConcertsByComposer(Model model, @RequestParam(name = "name", required = true) String name) {
        log.info("Listing all concerts containing composer " + name);
        List<Concert> concerts = concertRepository.findAllByComposer(name, Sort.by("date"));
        model.addAttribute("concerts", concerts);
        return "listConcerts";
    }
    @RequestMapping(path = "/listBySoloist", method = RequestMethod.GET)
    public String listConcertsBySoloist(Model model, @RequestParam(name = "id", required = true) Integer id) {
        log.info("Listing all concerts containing artist " + id);
        List<Concert> concerts = concertRepository.findAllBySoloist(id, Sort.by("date"));
        model.addAttribute("concerts", concerts);
        return "listConcerts";
    }
    @RequestMapping(path = "/listBySkill", method = RequestMethod.GET)
    public String listConcertsBySkill(Model model, @RequestParam(name = "name", required = true) String name) {
        log.info("Listing all concerts containing skill " + name);
        List<Concert> concerts = concertRepository.findAllBySkill(name, Sort.by("date"));
        model.addAttribute("concerts", concerts);
        return "listConcerts";
    }
    /**
     * Upload a simple text file
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
        while((entry = zis.getNextEntry()) != null) {
            log.debug("reading zip file " + entry.getName());
            BufferedReader r = new BufferedReader(new InputStreamReader(zis, getEncoding()));
            loadConcertFile(r);
        }
        return "redirect:/concert/list";
    }
    /**
     * Load a text file representing a concert
     * @param r
     * @throws IOException
     * @throws ParseException 
     */
    public void loadConcertFile(BufferedReader r) throws IOException, ParseException {
      
        String line;
        Venue venue = getVenue("Unknown");
        Artist conductor = getArtist("Unknown");
        Set<Engagement> soloists = new HashSet<>();
        Set<Piece> pieces = new HashSet<>();
        /*
         * Read the first line - BSO 2000-03-25 (Saturday)
         */
        line = r.readLine();
        StringTokenizer tok = new StringTokenizer(line, " ");
        tok.nextToken(); // Ignore first word   
        String concertDate = tok.nextToken();
        Date held = new Date(sdf.parse(concertDate).getTime());
        log.debug("parsed (" + concertDate + ") as " + DateFormat.getDateInstance().format(held));
        while ((line = r.readLine()) != null) {
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
             * Soloist is actually a comma-separated list
             * Also allow plural
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
                while ((line = r.readLine()) != null) {
                    log.debug("read " + line);
                    if (!line.contains(":")) {
                        break;
                    }
                    tok = new StringTokenizer(line, ":");
                    String composer = tok.nextToken().trim();
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
}
