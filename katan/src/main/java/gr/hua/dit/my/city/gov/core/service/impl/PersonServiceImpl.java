package gr.hua.dit.my.city.gov.core.service.impl;



import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.PersonType;
import gr.hua.dit.my.city.gov.core.port.PhoneNumberPort;
import gr.hua.dit.my.city.gov.core.port.SmsNotificationPort;
import gr.hua.dit.my.city.gov.core.port.impl.dto.PhoneNumberValidationResult;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.service.PersonService;

import gr.hua.dit.my.city.gov.core.service.mapper.PersonMapper;

import gr.hua.dit.my.city.gov.core.service.model.CreatePersonRequest;
import gr.hua.dit.my.city.gov.core.service.model.CreatePersonResult;

import gr.hua.dit.my.city.gov.core.service.model.PersonView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link PersonService}.
 */
@Service
public class PersonServiceImpl implements PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

    private final PasswordEncoder passwordEncoder;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final PhoneNumberPort phoneNumberPort;
    private final SmsNotificationPort smsNotificationPort;

    public PersonServiceImpl(final PasswordEncoder passwordEncoder,
                             final PersonRepository personRepository,
                             final PersonMapper personMapper,
                             final PhoneNumberPort phoneNumberPort,
                             final SmsNotificationPort smsNotificationPort) {
        if (passwordEncoder == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();
        if (phoneNumberPort == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();

        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.phoneNumberPort = phoneNumberPort;
        this.smsNotificationPort = smsNotificationPort;
    }

    @Override
    public CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify) {
        if (createPersonRequest == null) throw new NullPointerException();

        // Unpack (we assume valid `CreatePersonRequest` instance)
        // --------------------------------------------------

        final PersonType type = createPersonRequest.type();
        final String afm = createPersonRequest.afm().strip(); // remove whitespaces
        final String amka = createPersonRequest.amka().strip();
        final String firstName = createPersonRequest.firstName().strip();
        final String lastName = createPersonRequest.lastName().strip();
        final String emailAddress = createPersonRequest.emailAddress().strip();
        String PhoneNumber = createPersonRequest.phoneNumber().strip();
        final String rawPassword = createPersonRequest.rawPassword();

        // Basic email address validation.
        // --------------------------------------------------

        if (!emailAddress.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return CreatePersonResult.fail("Please provide a valid email address");
        }

        // Advanced mobile phone number validation.
        // --------------------------------------------------

        final PhoneNumberValidationResult phoneNumberValidationResult
            = this.phoneNumberPort.validate(PhoneNumber);
        if (!phoneNumberValidationResult.isValidMobile()) {
            return CreatePersonResult.fail("Phone Number is not valid");
        }
        PhoneNumber = phoneNumberValidationResult.e164();

        // --------------------------------------------------

        if (this.personRepository.existsByAfmIgnoreCase(afm)) {
            return CreatePersonResult.fail("AFM already registered");
        }

        if (this.personRepository.existsByEmailAddressIgnoreCase(emailAddress)) {
            return CreatePersonResult.fail("Email Address already registered");
        }

        if (this.personRepository.existsByPhoneNumber(PhoneNumber)) {
            return CreatePersonResult.fail("Phone Number already registered");
        }

        // --------------------------------------------------

        // AFM external lookup/verification temporarily removed — accept provided AFM.

        // --------------------------------------------------

        final String hashedPassword = this.passwordEncoder.encode(rawPassword);

        // Instantiate person.
        // --------------------------------------------------

        Person person = new Person();
        person.setId(null); // auto generated
        person.setAfm(afm);
        person.setAmka(amka);
        person.setType(type);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setPhoneNumber(PhoneNumber);
        person.setPasswordHash(hashedPassword);
        person.setCreatedAt(null); // auto generated.

        // Persist person (save/insert to database)
        // --------------------------------------------------

        person = this.personRepository.save(person);

        // --------------------------------------------------

        if (notify) {
            final String content = String.format(
                "You have successfully registered for My City Gov." +
                    "Use your email (%s) to log in.", emailAddress);
            final boolean sent = this.smsNotificationPort.sendSms(PhoneNumber, content);
            if (!sent) {
                LOGGER.warn("SMS send to {} failed!", PhoneNumber);
            }
        }

        // Map `Person` to `PersonView`.
        // --------------------------------------------------

        final PersonView personView = this.personMapper.convertPersonToPersonView(person);

        // --------------------------------------------------

        return CreatePersonResult.success(personView);
    }
}
