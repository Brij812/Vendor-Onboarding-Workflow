import type { VendorSubmissionPayload } from '../types/submission';



export type ExpectedOutcome = 'APPROVED' | 'PENDING' | 'REJECTED';



export type SamplePdfSet = 'nexora' | 'brightlayer' | 'helix-bank-mismatch' | 'blackstone';



export interface DemoScenario {

  id: string;

  label: string;

  description: string;

  expectedOutcome: ExpectedOutcome;

  primaryIssueCode?: string;

  samplePdfSet?: SamplePdfSet;

  attachMisSlottedSamplePdfs?: boolean;

  data: VendorSubmissionPayload;

}



const approvedVendor: VendorSubmissionPayload = {

  legalName: 'Nexora Grid Solutions Pvt Ltd',

  country: 'India',

  website: 'https://nexoragrid.co.in',

  contactEmail: 'finance@nexoragrid.co.in',

  taxId: '29NEXOR5678P1Z2',

  bankAccountHolderName: 'Nexora Grid Solutions Pvt Ltd',

  bankCountry: 'India',

  bankCode: 'KKBK0003344',

  bankAccountLast4: '8473',

  businessCategory: 'Infrastructure Services',

};



const missingDocumentCase: VendorSubmissionPayload = {

  legalName: 'ClearPath Analytics Pvt Ltd',

  country: 'India',

  website: 'https://clearpath-analytics.in',

  contactEmail: 'vendor@clearpath-analytics.in',

  taxId: '29CLEAR1234F1Z5',

  bankAccountHolderName: 'ClearPath Analytics Pvt Ltd',

  bankCountry: 'India',

  bankCode: 'HDFC0005678',

  bankAccountLast4: '4521',

  businessCategory: '',

};



const bankNameMismatch: VendorSubmissionPayload = {

  legalName: 'Helix Data Systems Pvt Ltd',

  country: 'India',

  website: 'https://helixdata.in',

  contactEmail: 'finance@helixdata.in',

  taxId: '29HELIX5678P1Z3',

  bankAccountHolderName: 'Helix Data Services',

  bankCountry: 'India',

  bankCode: 'HDFC0002468',

  bankAccountLast4: '9834',

  businessCategory: 'Software Services',

};



const duplicateVendor: VendorSubmissionPayload = {

  legalName: 'BrightLayer Technologies Pvt Ltd',

  country: 'India',

  website: 'https://brightlayer.in',

  contactEmail: 'finance@brightlayer.in',

  taxId: '29ABCDE1234F1Z5',

  bankAccountHolderName: 'BrightLayer Technologies Pvt Ltd',

  bankCountry: 'India',

  bankCode: 'HDFC0001234',

  bankAccountLast4: '4567',

  businessCategory: 'Software Services',

};



const blockedVendor: VendorSubmissionPayload = {

  legalName: 'Blackstone Imports',

  country: 'India',

  website: 'https://blackstone-imports.example',

  contactEmail: 'ops@blackstone-imports.example',

  taxId: '12BADXX9999Z1Z9',

  bankAccountHolderName: 'Blackstone Imports',

  bankCountry: 'India',

  bankCode: 'HDFC0009999',

  bankAccountLast4: '8834',

  businessCategory: 'Trading',

};



export const demoScenarios: DemoScenario[] = [

  {

    id: 'approved-vendor',

    label: 'Load Approved Vendor',

    description: 'Complete vendor data with matching sample PDFs attached.',

    expectedOutcome: 'APPROVED',

    samplePdfSet: 'nexora',

    data: approvedVendor,

  },

  {

    id: 'missing-document',

    label: 'Load Missing Document Case',

    description: 'All fields except business category; no PDFs uploaded.',

    expectedOutcome: 'PENDING',

    primaryIssueCode: 'MISSING_BUSINESS_CATEGORY',

    data: missingDocumentCase,

  },

  {

    id: 'bank-name-mismatch',

    label: 'Load Bank Name Mismatch Case',

    description: 'Complete data and PDFs; legal name and bank holder name differ.',

    expectedOutcome: 'PENDING',

    primaryIssueCode: 'BANK_NAME_MISMATCH',

    samplePdfSet: 'helix-bank-mismatch',

    data: bankNameMismatch,

  },

  {

    id: 'duplicate-vendor',

    label: 'Load Duplicate Vendor Case',

    description: 'Complete data and PDFs; tax ID matches seeded BrightLayer vendor.',

    expectedOutcome: 'REJECTED',

    primaryIssueCode: 'DUPLICATE_TAX_ID',

    samplePdfSet: 'brightlayer',

    data: duplicateVendor,

  },

  {

    id: 'blocked-vendor',

    label: 'Load Blocked Vendor Case',

    description: 'Complete data and PDFs; tax ID matches seeded blocked vendor.',

    expectedOutcome: 'REJECTED',

    primaryIssueCode: 'BLOCKED_VENDOR_MATCH',

    samplePdfSet: 'blackstone',

    data: blockedVendor,

  },

  {

    id: 'wrong-document-type',

    label: 'Load Wrong Document Uploaded Case',

    description: 'Complete Nexora data; bank proof PDF placed in the tax registration slot.',

    expectedOutcome: 'PENDING',

    primaryIssueCode: 'WRONG_DOCUMENT_TYPE',

    attachMisSlottedSamplePdfs: true,

    data: approvedVendor,

  },

];

