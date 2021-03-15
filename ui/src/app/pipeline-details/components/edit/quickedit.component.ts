/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output
} from "@angular/core";
import {PipelineService} from "../../../platform-services/apis/pipeline.service";
import {
    DataProcessorInvocation,
    DataSinkInvocation,
    EventSchema,
    Pipeline, PipelineOperationStatus
} from "../../../core-model/gen/streampipes-model";
import {PipelineElementUnion} from "../../../editor/model/editor.model";
import {FormBuilder, FormGroup} from "@angular/forms";
import {PipelineStatusDialogComponent} from "../../../pipelines/dialog/pipeline-status/pipeline-status-dialog.component";
import {PanelType} from "../../../core-ui/dialog/base-dialog/base-dialog.model";
import {DialogService} from "../../../core-ui/dialog/base-dialog/base-dialog.service";
import {DialogRef} from "../../../core-ui/dialog/base-dialog/dialog-ref";

@Component({
    selector: 'quick-edit',
    templateUrl: './quickedit.component.html',
})
export class QuickEditComponent implements OnInit, AfterViewInit{

    @Input()
    pipeline: Pipeline;

    @Output()
    reloadPipelineEmitter: EventEmitter<void> = new EventEmitter<void>();

    _selectedElement: PipelineElementUnion;

    eventSchemas: EventSchema[];

    parentForm: FormGroup;
    formValid: boolean;
    viewInitialized: boolean = false;

    isInvocable: boolean = false;
    isDataProcessor: boolean = false;

    pipelineUpdating: boolean = false;
    pipelineReconfiguration: boolean = false;

    constructor(private pipelineService: PipelineService,
                private fb: FormBuilder,
                private changeDetectorRef: ChangeDetectorRef,
                private dialogService: DialogService) {

    }

    ngOnInit() {
        this.parentForm = this.fb.group({
        });

        this.parentForm.statusChanges.subscribe((status)=>{
            this.formValid = this.viewInitialized && this.parentForm.valid;
        })
    }

    ngAfterViewInit(): void {
        this.viewInitialized = true;
        this.formValid = this.viewInitialized && this.parentForm.valid;
        this.changeDetectorRef.detectChanges();
    }

    updatePipeline() {
        this.pipelineUpdating = true;
        this.updatePipelineElement();
        this.pipelineService.updatePipeline(this.pipeline).subscribe(data => {
            this.reloadPipelineEmitter.emit();
            this.pipelineUpdating = false;
        });
    }

    updatePipelineElement() {
        if (this._selectedElement instanceof DataProcessorInvocation) {
            this.updateDataProcessor();
        } else if (this._selectedElement instanceof DataSinkInvocation) {
            this.updateDataSink();
        }
    }

    updateDataProcessor() {
        let dataProcessors: DataProcessorInvocation[] = [];
        this.pipeline.sepas.forEach(p => {
           if (p.dom === this._selectedElement.dom) {
               dataProcessors.push(this._selectedElement as DataProcessorInvocation);
           } else {
                dataProcessors.push(p);
           }
        });
        this.pipeline.sepas = dataProcessors;
    }

    updateDataSink() {
        let dataSinks: DataSinkInvocation[] = [];
        this.pipeline.actions.forEach(p => {
            if (p.dom === this._selectedElement.dom) {
                dataSinks.push(this._selectedElement as DataSinkInvocation);
            } else {
                dataSinks.push(p);
            }
        });
        this.pipeline.actions = dataSinks;
    }

    get selectedElement() {
        return this._selectedElement;
    }

    @Input()
    set selectedElement(selectedElement: PipelineElementUnion) {
        if (this._selectedElement) {
            this.updatePipelineElement();
        }
        this._selectedElement = selectedElement;
        this.eventSchemas = [];
        if (this._selectedElement instanceof DataProcessorInvocation || this._selectedElement instanceof DataSinkInvocation) {
            (this._selectedElement as any).inputStreams.forEach(is => {
                this.eventSchemas = this.eventSchemas.concat(is.eventSchema);
            });
        }
        this.updateTypeInfo();
    }

    updateTypeInfo() {
        this.isDataProcessor = this._selectedElement instanceof DataProcessorInvocation;
        this.isInvocable = this._selectedElement instanceof DataProcessorInvocation ||
            this._selectedElement instanceof DataSinkInvocation;
    }

    reconfigurePipeline() {
        this.pipelineReconfiguration = true;
        this.updatePipelineElement();
        this.pipelineService.reconfigurePipeline(this.pipeline).subscribe(statusMessage => {
            if (statusMessage.success) {
                this.afterReconfiguration(statusMessage, this.pipeline._id)
                this.reloadPipelineEmitter.emit();
                this.pipelineReconfiguration = false;
            } else {
                this.displayErrors(statusMessage);
            }
        }, data => {
            this.displayErrors();
        });
    }

    afterReconfiguration(statusMessage: PipelineOperationStatus, pipelineId?: string) {
        this.showDialog(statusMessage);
    }

    showDialog(data: PipelineOperationStatus) {
        this.dialogService.open(PipelineStatusDialogComponent, {
            panelType: PanelType.STANDARD_PANEL,
            title: "Pipeline Status",
            width: "70vw",
            data: {
                "pipelineOperationStatus": data
            }
        });
    };

    displayErrors(statusMessage?: PipelineOperationStatus) {
        this.showDialog(statusMessage);
    }
}

