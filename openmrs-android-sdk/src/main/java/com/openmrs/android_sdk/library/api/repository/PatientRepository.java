/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package com.openmrs.android_sdk.library.api.repository;

import static android.content.ContentValues.TAG;
import static com.openmrs.android_sdk.library.databases.AppDatabaseHelper.createObservableIO;
import static com.openmrs.android_sdk.utilities.ApplicationConstants.PRIMARY_KEY_ID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import rx.Observable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmrs.android_sdk.library.OpenmrsAndroid;
import com.openmrs.android_sdk.library.api.RestApi;
import com.openmrs.android_sdk.library.api.RestServiceBuilder;
import com.openmrs.android_sdk.library.api.workers.UpdatePatientWorker;
import com.openmrs.android_sdk.library.dao.EncounterCreateRoomDAO;
import com.openmrs.android_sdk.library.dao.PatientDAO;
import com.openmrs.android_sdk.library.databases.AppDatabaseHelper;
import com.openmrs.android_sdk.library.models.CallTokenModel;
import com.openmrs.android_sdk.library.models.CustomIdGenPatientIdentifiers;
import com.openmrs.android_sdk.library.models.CustomPatientIdentifier;
import com.openmrs.android_sdk.library.models.Encountercreate;
import com.openmrs.android_sdk.library.models.IdGenPatientIdentifiers;
import com.openmrs.android_sdk.library.models.IdentifierType;
import com.openmrs.android_sdk.library.models.Module;
import com.openmrs.android_sdk.library.models.Patient;
import com.openmrs.android_sdk.library.models.PatientCreateDTO;
import com.openmrs.android_sdk.library.models.PatientDto;
import com.openmrs.android_sdk.library.models.PatientDtoUpdate;
import com.openmrs.android_sdk.library.models.PatientIdentifier;
import com.openmrs.android_sdk.library.models.PatientPhoto;
import com.openmrs.android_sdk.library.models.PatientSaveDTO;
import com.openmrs.android_sdk.library.models.Person;
import com.openmrs.android_sdk.library.models.PersonAttribute;
import com.openmrs.android_sdk.library.models.RTCToken;
import com.openmrs.android_sdk.library.models.ReferredPatient;
import com.openmrs.android_sdk.library.models.ReferredPatientResponse;
import com.openmrs.android_sdk.library.models.ResultType;
import com.openmrs.android_sdk.library.models.Results;
import com.openmrs.android_sdk.library.models.SearchRequest;
import com.openmrs.android_sdk.library.models.SearchUser;
import com.openmrs.android_sdk.library.models.SearchUserResponse;
import com.openmrs.android_sdk.library.models.SystemProperty;
import com.openmrs.android_sdk.library.models.TestResponse;
import com.openmrs.android_sdk.library.models.TextBody;
import com.openmrs.android_sdk.utilities.ApplicationConstants;
import com.openmrs.android_sdk.utilities.ModuleUtils;
import com.openmrs.android_sdk.utilities.NetworkUnavailableException;
import com.openmrs.android_sdk.utilities.NetworkUtils;
import com.openmrs.android_sdk.utilities.PatientComparator;
import com.openmrs.android_sdk.utilities.ToastUtil;

/**
 * The type Patient repository.
 */
@Singleton
public class PatientRepository extends BaseRepository {
    private final PatientDAO patientDAO;
    private final LocationRepository locationRepository;
    private final EncounterRepository encounterRepository;

    /**
     * Instantiates a new Patient repository.
     */
    @Inject
    public PatientRepository(PatientDAO patientDAO, LocationRepository locationRepository,
                             EncounterRepository encounterRepository) {
        this.patientDAO = patientDAO;
        this.locationRepository = locationRepository;
        this.encounterRepository = encounterRepository;
    }

    /**
     * Uploads a patient to the server.
     *
     * @param patient the patient to be registered in the server
     */
    public Observable<Patient> syncPatient(final Patient patient) {
        return createObservableIO(() -> {
            final List<PatientIdentifier> identifiers = new ArrayList<>();
            final PatientIdentifier identifier = new PatientIdentifier();
            identifier.setLocation(locationRepository.getLocation());
            identifier.setIdentifier(getIdGenPatientIdentifier());
            identifier.setIdentifierType(getPatientIdentifierType());
            identifiers.add(identifier);

            patient.setIdentifiers(identifiers);

            PatientDto patientDto = patient.getPatientDto();

            Response<PatientDto> response = restApi.createPatient(patientDto).execute();
            if (response.isSuccessful()) {
                PatientDto returnedPatientDto = response.body();

                patient.setUuid(returnedPatientDto.getUuid());
                if (patient.getPhoto() != null) {
                    uploadPatientPhoto(patient);
                }

                patientDAO.updatePatient(patient.getId(), patient);
                if (!patient.getEncounters().equals("")) {
                    addEncounters(patient);
                }

                return patient;
            } else {
                throw new Exception("syncPatient error: " + response.message());
            }
        });
    }

    /*public Observable<Patient> syncPatient(final Patient patient, final PatientCreateDTO patientCreateDTO) {
        return createObservableIO(() -> {
            final List<CustomPatientIdentifier> identifiers = new ArrayList<>();
            final CustomPatientIdentifier identifier = new CustomPatientIdentifier();

            String location = Objects.requireNonNull(locationRepository.getLocation()).getUuid();
            identifier.setLocation(location);

            String mIdentifier = getCustomPatientIdentifier();
            identifier.setIdentifier(mIdentifier);

            String mIdentifierType = getPatientIdentifierType().getUuid();
            identifier.setIdentifierType(mIdentifierType);

            identifiers.add(identifier);

            patientCreateDTO.setIdentifiers(identifiers);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String mjson = gson.toJson(patientCreateDTO);

            Response<PatientDto> response = restApi.createPatientDTO(patientCreateDTO).execute();
            if (response.isSuccessful()) {
                PatientDto returnedPatientDto = response.body();
                if (returnedPatientDto != null) {
                    patient.setUuid(returnedPatientDto.getUuid());
                    patient.getPerson().setAttributes(returnedPatientDto.getPerson().getAttributes());
                    patient.setIdentifiers(returnedPatientDto.getIdentifiers());
//                    patientDAO.updatePatient(patient.getId(), patient);

                    if (!patient.getEncounters().isEmpty()) {
                        addEncounters(patient);
                    }

                    Gson gsons = new GsonBuilder().setPrettyPrinting().create();
                    String mjsons = gsons.toJson(returnedPatientDto);
                    PatientSaveDTO psDTO = toSavePatientDTO(returnedPatientDto);
                    savePatient(psDTO).single().toBlocking().first();
                }
            } else {
                throw new Exception("syncPatient error: " + response.message());
            }
            return patient;
        });
    }*/

    public Observable<PatientDto> syncPatient(final Patient patient, final PatientCreateDTO patientCreateDTO) {
        return createObservableIO(() -> {
            final List<CustomPatientIdentifier> identifiers = new ArrayList<>();
            final CustomPatientIdentifier identifier = new CustomPatientIdentifier();

            String location = Objects.requireNonNull(locationRepository.getLocation()).getUuid();
            identifier.setLocation(location);

            String mIdentifier = getCustomPatientIdentifier();
            identifier.setIdentifier(mIdentifier);

            String mIdentifierType = getPatientIdentifierType().getUuid();
            identifier.setIdentifierType(mIdentifierType);

            identifiers.add(identifier);

            patientCreateDTO.setIdentifiers(identifiers);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String mjson = gson.toJson(patientCreateDTO);

            Response<PatientDto> response = restApi.createPatientDTO(patientCreateDTO).execute();
            if (response.isSuccessful()) {
                PatientDto returnedPatientDto = response.body();
                /*patient.setUuid(returnedPatientDto.getUuid());
                patient.getPerson().setAttributes(Objects.requireNonNull(returnedPatientDto.getPerson()).getAttributes());
                patient.setPerson(returnedPatientDto.getPerson());
                patient.setIdentifiers(returnedPatientDto.getIdentifiers());
                patientDAO.updatePatient(patient.getId(), patient);*/

//                if (!patient.getEncounters().isEmpty()) {
//                    addEncounters(patient);
//                }

                return returnedPatientDto;
            } else {
                throw new Exception("syncPatient error: " + response.message());
            }
        });
    }

    public Observable<TestResponse> savePatient(final PatientSaveDTO psDTO) {
        return createObservableIO(() -> {
            try{
//                Call<TestResponse> call = restApi.savePatientDTO("application/json", psDTO);
                Call<TestResponse> call = restApi.savePatientDTO(psDTO);
                Response<TestResponse> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();
                } else {
                    String aa = response.message();
                    throw new Exception("save patient error: " + aa);
                }
            } catch (Exception ex) {
                throw new Exception("save patient error: " + ex.toString());
            }
        });
    }

    public PatientSaveDTO toSavePatientDTO(PatientDto rpDTO) {
        PatientSaveDTO psDTO = new PatientSaveDTO();
        psDTO.setUuid(rpDTO.getUuid());
        psDTO.setDisplay(Objects.requireNonNull(rpDTO.getPerson()).getDisplay());
        psDTO.setGender(rpDTO.getPerson().getGender());
        psDTO.setAge(rpDTO.getPerson().getAge());
        psDTO.setBirthdate(rpDTO.getPerson().getBirthdate());
        psDTO.setAttributes(rpDTO.getPerson().getAttributes());
        psDTO.setPersonUUID(rpDTO.getPerson().getUuid());
        psDTO.setIdentifier(parseAttributeValue(rpDTO.getIdentifiers().get(0).getDisplay(), "=").get(1));
        if(rpDTO.getPerson().getNames().size() > 0){
            psDTO.setFirstName(rpDTO.getPerson().getNames().get(0).getGivenName());
            psDTO.setLastName(rpDTO.getPerson().getNames().get(0).getMiddleName() + rpDTO.getPerson().getNames().get(0).getFamilyName());
        } else {
            ArrayList<String> finalList = parseAttributeValue(rpDTO.getPerson().getDisplay(), " ");
            psDTO.setFirstName(finalList.get(0));
            psDTO.setLastName(finalList.get(1));
        }
        psDTO.setCountryId(0);
        psDTO.setLocation(0);
        psDTO.setBlockId(0);
        psDTO.setDeathdateEstimated(false);
        for (PersonAttribute pa : rpDTO.getPerson().getAttributes()) {
            ArrayList<String> values = parseAttributeValue(pa.getDisplay(), "=");
            String attrUUID = values.get(0).trim();
            String value = values.get(1);
//            String attrUUID = pa.getUuid();
            if(attrUUID.equals(ApplicationConstants.PATIENTS_BIRTH_PLACE_KEY)){
                psDTO.setBirthPlace(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_MOBILE_KEY)){
                psDTO.setMobile(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_NID_KEY)){
                psDTO.setNid(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_MOTHER_NAME_KEY)){
                psDTO.setMotherName(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_FATHER_NAME_KEY)){
                psDTO.setFatherName(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_MOTHER_NAME_BANGLA_KEY)){
                psDTO.setMotherNameBangla(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_FATHER_NAME_BANGLA_KEY)){
                psDTO.setFatherNameBangla(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_NATIONALITY_KEY)){
                psDTO.setNationality(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_OCCUPATION_KEY)){
                psDTO.setOccupation(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_RELIGION_KEY)){
                psDTO.setRelegion(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_DIVISION_KEY)){
                psDTO.setDivision(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_BLOOD_GROUP_KEY)){
                psDTO.setBloodGroup(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_EDUCATION_KEY)){
                psDTO.setEduQualification(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_MARITAL_STATUS_KEY)){
                psDTO.setMatritalStatus(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_DISABILITY_TYPE_KEY)){
                psDTO.setDisabilityType(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_ETHNICITY_KEY)){
                psDTO.setEthnicity(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_FULL_NAME_BANGLA_KEY)){
                psDTO.setFullNameBangla(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_SPOUSE_NAME_BANGLA_KEY)){
                psDTO.setSpouseNameBangla(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_SPOUSE_NAME_ENGLISH_KEY)){
                psDTO.setSpouseNameEnglish(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_DISTRICT_KEY)){
                psDTO.setDistrict(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_UPAZILA_KEY)){
                psDTO.setUpazila(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_PAURASAVA_KEY)){
                psDTO.setPaurasava(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_UNION_KEY)){
                psDTO.setUnionName(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_WARD_KEY)){
                psDTO.setWard(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_ADDRESS_KEY)){
                psDTO.setPatientAddress(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_UNION_ID_KEY)){
                psDTO.setUnionId(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_DIVISION_ID_KEY)){
                psDTO.setDivisionId(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_DISTRICT_ID_KEY)){
                psDTO.setDistrictId(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_UPAZILA_ID_KEY)){
                psDTO.setUpazilaId(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_PAURASAVA_ID_KEY)){
                psDTO.setPaurasavaId(value);
            } else if(attrUUID.equals(ApplicationConstants.PATIENTS_WARD_ID_KEY)){
                psDTO.setWardId(value);
            }
        }
        return psDTO;
    }

    ArrayList<String> parseAttributeValue(String givenValue, String indicator){
        ArrayList<String> finalList = new ArrayList<>();
        int charIndex = givenValue.indexOf(indicator);
        if (charIndex != -1) {
            String beforeChar = givenValue.substring(0, charIndex);
            String afterChar = givenValue.substring(charIndex + 1).trim();
            finalList.add(beforeChar);
            finalList.add(afterChar);
        } else {
            Log.d("Parse attribute", "Character not found in string");
        }
        return finalList;
    }


    private void uploadPatientPhoto(final Patient patient) {
        PatientPhoto patientPhoto = new PatientPhoto();
        patientPhoto.setPhoto(patient.getPhoto());
        patientPhoto.setPerson(patient);
        Call<PatientPhoto> personPhotoCall =
                restApi.uploadPatientPhoto(patient.getUuid(), patientPhoto);
        personPhotoCall.enqueue(new Callback<PatientPhoto>() {
            @Override
            public void onResponse(@NonNull Call<PatientPhoto> call, @NonNull Response<PatientPhoto> response) {
                if (!response.isSuccessful()) {
                    getLogger().e(response.message());
                    //string resource added "patient_photo_update_unsuccessful"
                    ToastUtil.error("Patient photo cannot be synced due to server error " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PatientPhoto> call, @NonNull Throwable t) {
                getLogger().e(t.getMessage());
                //string resource added "patient_photo_update_unsuccessful"
                ToastUtil.error("Patient photo cannot be synced due to server error " + t.toString());
            }
        });
    }

    /**
     * Registers a patient locally or to the server, according to network state.
     *
     * @param patient the patient to be registered
     * @return Observable result type of registration process
     */
    public Observable<Patient> registerPatient(final Patient patient) {
        return createObservableIO(() -> {
            Long id = patientDAO.savePatient(patient).single().toBlocking().first();
            patient.setId(id);
            if (NetworkUtils.isOnline()) syncPatient(patient).single().toBlocking().first();
            return patient;
        });
    }

    /*public Observable<Patient> registerPatient(final Patient patient, final PatientCreateDTO patientCreateDTO) {
        return createObservableIO(() -> {
            Long id = patientDAO.savePatient(patient).single().toBlocking().first();
            patient.setId(id);
            return patient;
        })
                .flatMap(savedPatient -> {
                    if (NetworkUtils.isOnline()) {
                        return syncPatient(savedPatient, patientCreateDTO);
                    } else {
                        return Observable.error(new IOException("Network is not available"));
                    }
                })
                .flatMap(returnedPatientDto -> {
                    try{
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        PatientSaveDTO psDTO = toSavePatientDTO(returnedPatientDto);
                        String mjson = gson.toJson(psDTO);
                        return savePatient(psDTO)
                                .map(responseBody -> {
                            patient.setUuid(returnedPatientDto.getUuid());
                            return patient;
                        });
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }*/

    public Observable<Patient> registerPatient(final Patient patient, final PatientCreateDTO patientCreateDTO) {
        return createObservableIO(() -> {
            /*Long id = patientDAO.savePatient(patient).single().toBlocking().first();
            patient.setId(id);*/
            return patient;
        }).flatMap(savedPatient -> {
            if (NetworkUtils.isOnline()) {
                return syncPatient(savedPatient, patientCreateDTO)
                        .flatMap(returnedPatientDto -> {
                            try {
                                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                PatientSaveDTO psDTO = toSavePatientDTO(returnedPatientDto);
                                String mjson = gson.toJson(psDTO);

                                return savePatient(psDTO)
                                        .map(responseBody -> {
                                            patient.setUuid(returnedPatientDto.getUuid());
                                            return patient;
                                        });
                            } catch (Exception ex) {
                                return Observable.error(new RuntimeException(ex));
                            }
                        });
            } else {
                return Observable.error(new NetworkUnavailableException("Network is not available, data saved in local"));
            }
        });
    }

    /*public Observable<Patient> postToServer(final Patient patient, final PatientCreateDTO patientCreateDTO) {
        return createObservableIO(() -> syncPatient(patient, patientCreateDTO)
                .flatMap(returnedPatientDto -> {
                    try {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        PatientSaveDTO psDTO = toSavePatientDTO(returnedPatientDto);
                        return savePatient(psDTO)
                                .map(responseBody -> {
                                    patient.setUuid(returnedPatientDto.getUuid());
                                    return patient;
                                });
                    } catch (Exception ex) {
                        return Observable.error(new NetworkUnavailableException("Network is not available, data saved in local"));
                    }
                }));
    }*/

    public Observable<List<Patient>> fetchUnSyncedPatients() {
        return createObservableIO(patientDAO::getUnSyncedPatients);
    }

    /**
     * Updates patient locally and remotely.
     *
     * @param patient the patient
     * @return Observable result type
     */
    public Observable<ResultType> updatePatient(final Patient patient) {
        return createObservableIO(() -> {
            if (NetworkUtils.isOnline()) {
                Call<PatientDto> call = restApi.updatePatient(
                        patient.getUpdatedPatientDto(), patient.getUuid(), "full");
                Response<PatientDto> response = call.execute();

                if (response.isSuccessful()) {
                    PatientDto patientDto = response.body();
                    patient.setBirthdate(patientDto.getPerson().getBirthdate());
                    patient.setUuid(patientDto.getUuid());

                    if (patient.getPhoto() != null) uploadPatientPhoto(patient);

                    patientDAO.updatePatient(patient.getId(), patient);

                    return ResultType.PatientUpdateSuccess;
                } else {
                    throw new Exception("updatePatient error: " + response.message());
                }
            } else {
                patientDAO.updatePatient(patient.getId(), patient);

                Data data = new Data.Builder().putString(PRIMARY_KEY_ID, patient.getId().toString()).build();
                Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                getWorkManager().enqueue(new OneTimeWorkRequest.Builder(UpdatePatientWorker.class).setConstraints(constraints).setInputData(data).build());

                return ResultType.PatientUpdateLocalSuccess;
            }
        });
    }

    /**
     * Update matching patient.
     *
     * @param patient the locally merged patient
     */
    public Observable<Patient> updateMatchingPatient(final Patient patient) {
        return createObservableIO(() -> {

            PatientDtoUpdate patientDto = patient.getUpdatedPatientDto();

            Call<PatientDto> call = restApi.updatePatient(patientDto, patient.getUuid(), ApplicationConstants.API.FULL);
            Response<PatientDto> response = call.execute();

            if (response.isSuccessful()) return patient;
            else throw new IOException(response.message());
        });
    }

    /**
     * Download patient by uuid.
     *
     * @param uuid patient uuid
     * @return Patient observable
     */
    public Observable<Patient> downloadPatientByUuid(@NonNull final String uuid) {
        return createObservableIO(() -> {
            Call<PatientDto> call = restApi.getPatientByUUID(uuid, "full");
            Response<PatientDto> response = call.execute();
            if (response.isSuccessful()) {
                final PatientDto newPatientDto = response.body();

                Bitmap photo = downloadPatientPhotoByUuid(newPatientDto.getUuid()).toBlocking().first();
                if (photo != null) newPatientDto.getPerson().setPhoto(photo);

                return newPatientDto.getPatient();
            } else {
                throw new IOException("Error with downloading patient: " + response.message());
            }
        });
    }

    /**
     * Download patient photo by uuid.
     *
     * @param uuid patient uuid
     * @return Photo bitmap or null bitmap observable
     */
    public Observable<Bitmap> downloadPatientPhotoByUuid(String uuid) {
        return createObservableIO(() -> {
            Call<ResponseBody> call = restApi.downloadPatientPhoto(uuid);
            Response<ResponseBody> response = call.execute();

            if (response.isSuccessful()) {
                try {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    return bitmap;
                } catch (Exception e) {
                    getLogger().e(e.getMessage());
                }
            }
            return null;
        });
    }

    /**
     * Add encounters.
     *
     * @param patient the patient
     */
    public void addEncounters(Patient patient) {
        EncounterCreateRoomDAO dao = db.encounterCreateRoomDAO();
        String enc = patient.getEncounters();
        List<Long> list = new ArrayList<>();
        for (String s : enc.split(","))
            list.add(Long.parseLong(s));

        for (long id : list) {
            Encountercreate encountercreate = dao.getCreatedEncountersByID(id);
            encountercreate.setPatient(patient.getUuid());
            encountercreate.setSynced(false);
            encounterRepository.updateEncounterCreate(encountercreate);
        }
    }

    /**
     * Gets id gen patient identifier.
     *
     * @return the id gen patient identifier
     */
    public String getIdGenPatientIdentifier() throws IOException {
        IdGenPatientIdentifiers idList = null;

        RestApi patientIdentifierService = RestServiceBuilder.createServiceForPatientIdentifier(RestApi.class);

        Call<IdGenPatientIdentifiers> call = patientIdentifierService.getPatientIdentifiers(OpenmrsAndroid.getUsername(), OpenmrsAndroid.getPassword());

        Response<IdGenPatientIdentifiers> response = call.execute();
        if (response.isSuccessful()) {
            idList = response.body();
        }

        return idList.getIdentifiers().get(0);
    }

    /**
     * Get custom patient identifier
     *
     * @return patient identifier type
     */
    public String getCustomPatientIdentifier() throws IOException {
        String customIdentifier = null;

        Map<String, Object> searchBody = new HashMap<>();
        Call<CustomIdGenPatientIdentifiers> call = restApi.getPatientIdentifiersCustom("8549f706-7e85-4c1d-9424-217d50a2988b",  searchBody);

        Response<CustomIdGenPatientIdentifiers> response = call.execute();
        if (response.isSuccessful()) {
            assert response.body() != null;
            customIdentifier = response.body().getIdentifier();
        }

        return customIdentifier;
    }

    /**
     * Gets patient identifier type (only has uuid).
     *
     * @return the patient identifier type
     */
    public IdentifierType getPatientIdentifierType() throws IOException {
        Call<Results<IdentifierType>> call = restApi.getIdentifierTypes();
        Response<Results<IdentifierType>> response = call.execute();
        if (response.isSuccessful()) {
            Results<IdentifierType> idResList = response.body();
            for (IdentifierType result : idResList.getResults()) {
                if (result.getDisplay().equals("OpenMRS ID")) {
                    return result;
                }
            }
        }
        return null;
    }

    public Observable<SearchUser> getUserBySearchIdentifier(SearchRequest searchBody) {
        return createObservableIO(() -> {
            Call<SearchUserResponse> call = restApi.getUserBySearch(searchBody);
            Response<SearchUserResponse> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().getSearchUser();
            } else {
                throw new Exception("Error with searching user by identifier: " + response.message());
            }
        });
    }

    public Observable<RTCToken> getGeneratedToken(CallTokenModel ctm) {
        return AppDatabaseHelper.createObservableIO(() -> {
            String endURL = ApplicationConstants.RTC_SERVER_URL + "/api/getToken";
            Call<RTCToken> call = restApi.getCallToken(endURL, ctm.getDid(), ctm.getTid(), ctm.getPid());
            try{
                Response<RTCToken> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                } else {
                    throw new Exception("Error with generating call token: " + response.message());
                }
            }catch (Exception e){
                throw new Exception("Error with generating call token: ");
            }
        });
    }

    /**
     * Find patients.
     *
     * @param query patient query string
     * @return observable list of patients with matching query
     */
    public Observable<List<ReferredPatient>> findPatients(String query) {
        return createObservableIO(() -> {
            /*Response<Results<Patient>> response = restApi.getPatients(query, ApplicationConstants.API.FULL).execute();
            List<Patient> pList = response.body().getResults();*/
            TextBody requestBody = new TextBody(query);
            Call<ResponseBody> call = restApi.getSyncedPatients(requestBody);
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                String rawJson = response.body().string();
                return new Gson().fromJson(rawJson, ReferredPatientResponse.class).getPersons();
            } else {
                throw new Exception("Error with finding patients: " + response.message());
            }
        });
    }



    public Observable<Person> findPatientDetails(String patientUUID) {
        return createObservableIO(() -> {
            Call<ResponseBody> call = restApi.findPatientByUUID(patientUUID);
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                String rawJson = response.body().string();
                return new Gson().fromJson(rawJson, Person.class);
            } else {
                throw new Exception("Error fetching patient details: " + response.message());
            }
        });
    }

    public Observable<List<ReferredPatient>> findReferredPatients(String query) {
        return createObservableIO(() -> {
            TextBody requestBody = new TextBody(query);
            Call<ResponseBody> call = restApi.getReferredPatients(requestBody);
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                String rawJson = response.body().string();
                return new Gson().fromJson(rawJson, ReferredPatientResponse.class).getPersons();
            } else {
                throw new Exception("Error with finding referred patients: " + response.message());
            }
        });
    }


    /**
     * Load more patients.
     *
     * @param limit      the limit
     * @param startIndex the start index
     * @return observable list of last viewed patients
     */
    public Observable<Results<Patient>> loadMorePatients(int limit, int startIndex) {
        return createObservableIO(() -> {
            Call<Results<Patient>> call = restApi.getLastViewedPatients(limit, startIndex);
            Response<Results<Patient>> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw new Exception("Error with loading last viewed patients: " + response.message());
            }
        });
    }

    /**
     * Gets cause of death global id.
     *
     * @return Observable string UUID for cause of death Concept
     */
    public Observable<String> getCauseOfDeathGlobalConceptID() {
        return createObservableIO(() -> {
            Call<Results<SystemProperty>> call = restApi.getSystemProperty(ApplicationConstants.CAUSE_OF_DEATH, ApplicationConstants.API.FULL);
            Response<Results<SystemProperty>> response = call.execute();
            if (response.isSuccessful()) {
                return response.body().getResults().get(0).getConceptUUID();
            } else {
                throw new Exception("Error with fetching Cause of Death Concept: " + response.message());
            }
        });
    }

    /**
     * Fetches similar patients by different strategies:
     * <br> 1. Fetch similar patients from server directly using an API.
     * <br> 2. Fetch patients with similar names, then compare their other similarities locally.
     * <br> 3. Fetch locally saved patients, then compare their similarities.
     *
     * @param patient to find similar patients toSERVER_URL_RTC
     * @return Observable list of similar patients
     */
    public Observable<List<Patient>> fetchSimilarPatients(final Patient patient) {
        return createObservableIO(() -> {
            if (!NetworkUtils.isOnline()) {
                List<Patient> localPatients = patientDAO.getAllPatients().toBlocking().first();
                return new PatientComparator().findSimilarPatient(localPatients, patient);
            }

            Call<Results<Module>> moduleCall = restApi.getModules(ApplicationConstants.API.FULL);
            Response<Results<Module>> response = moduleCall.execute();

            if (!response.isSuccessful()) return fetchSimilarPatientsAndCalculateLocally(patient);

            if (ModuleUtils.isRegistrationCore1_7orAbove(response.body().getResults())) {
                //return fetchSimilarPatientsFromServer(patient); //Uncomment this line when server API is fixed
                return fetchSimilarPatientsAndCalculateLocally(patient); //Remove this line when server API is fixed
            } else {
//                ToastUtil.notifyLong(context.getString(R.string.registration_core_info));
                return fetchSimilarPatientsAndCalculateLocally(patient);
            }
        });
    }

    /**
     * Fetches similar patients directly from server.
     *
     * @param patient the patient to fetch similar patient to
     * @return list of similar patients
     */
    private List<Patient> fetchSimilarPatientsFromServer(final Patient patient) throws Exception {
        Call<Results<Patient>> call = restApi.getSimilarPatients(patient.toMap());
        Response<Results<Patient>> response = call.execute();
        if (response.isSuccessful()) return response.body().getResults();
        else throw new Exception("fetchSimilarPatientsFromServer error: " + response.message());
    }

    /**
     * Fetches patients with similar names from server, then calculates other similarities locally.
     *
     * @param patient the patient to fetch similar patient to
     * @return list of similar patients
     */
    private List<Patient> fetchSimilarPatientsAndCalculateLocally(final Patient patient) throws Exception {
        Call<Results<PatientDto>> call = restApi.getPatientsDto(patient.getName().getGivenName(), ApplicationConstants.API.FULL);
        Response<Results<PatientDto>> response = call.execute();
        if (response.isSuccessful()) {
            List<Patient> patientList = new ArrayList<>();
            for (PatientDto p : response.body().getResults()) patientList.add(p.getPatient());
            return new PatientComparator().findSimilarPatient(patientList, patient);
        } else {
            throw new Exception("fetchSimilarPatientAndCalculateLocally error: " + response.message());
        }
    }
}

