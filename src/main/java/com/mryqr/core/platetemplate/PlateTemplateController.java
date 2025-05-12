package com.mryqr.core.platetemplate;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.common.validation.platetemplateld.PlateTemplateId;
import com.mryqr.core.platetemplate.command.CreatePlateTemplateCommand;
import com.mryqr.core.platetemplate.command.PlateTemplateCommandService;
import com.mryqr.core.platetemplate.command.UpdatePlateTemplateCommand;
import com.mryqr.core.platetemplate.query.PlateTemplateQueryService;
import com.mryqr.core.platetemplate.query.QListPlateTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.mryqr.core.common.utils.ReturnId.returnId;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/plate-templates")
public class PlateTemplateController {
    private final PlateTemplateCommandService plateTemplateCommandService;
    private final PlateTemplateQueryService plateTemplateQueryService;

    @PostMapping
    @ResponseStatus(CREATED)
    public ReturnId createPlateTemplate(@RequestBody @Valid CreatePlateTemplateCommand command,
                                        @AuthenticationPrincipal User user) {
        String plateTemplateId = plateTemplateCommandService.createPlateTemplate(command, user);
        return returnId(plateTemplateId);
    }

    @PutMapping(value = "/{id}")
    public void updatePlateTemplate(@PathVariable("id") @NotBlank @PlateTemplateId String plateTemplateId,
                                    @RequestBody @Valid UpdatePlateTemplateCommand command,
                                    @AuthenticationPrincipal User user) {
        plateTemplateCommandService.updatePlateTemplate(plateTemplateId, command, user);
    }

    @DeleteMapping(value = "/{id}")
    public void deletePlateTemplate(@PathVariable("id") @NotBlank @PlateTemplateId String plateTemplateId,
                                    @AuthenticationPrincipal User user) {
        plateTemplateCommandService.deletePlateTemplate(plateTemplateId, user);
    }

    @GetMapping
    public List<QListPlateTemplate> listPlateTemplates() {
        return plateTemplateQueryService.listAllPlateTemplates();
    }

}
